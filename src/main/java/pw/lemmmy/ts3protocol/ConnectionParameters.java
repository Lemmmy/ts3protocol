package pw.lemmmy.ts3protocol;

import lombok.Getter;
import lombok.Setter;
import pw.lemmmy.ts3protocol.crypto.CachedKey;
import pw.lemmmy.ts3protocol.crypto.Crypto;
import pw.lemmmy.ts3protocol.packets.PacketDirection;
import pw.lemmmy.ts3protocol.packets.PacketType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static pw.lemmmy.ts3protocol.packets.PacketDirection.SERVER_TO_CLIENT;

@Getter
public class ConnectionParameters {
	private Client client;
	
	// temporary key/nonce until the handshake is complete
	private byte[] eaxKey = Crypto.FAKE_EAX_KEY;
	private byte[] eaxNonce = Crypto.FAKE_EAX_NONCE;
	
	@Setter private byte[] sharedIV;
	@Setter private byte[] sharedMac;
	
	protected short clientID = 0;
	
	private Map<PacketType, Integer> 	packetIDCounterIncoming = new HashMap<>(),
										packetIDCounterOutgoing = new HashMap<>(),
										packetGenerationCounterIncoming = new HashMap<>(),
										packetGenerationCounterOutgoing = new HashMap<>();
	private Map<PacketType, CachedKey> 	keyCacheIncoming = new HashMap<>(),
										keyCacheOutgoing = new HashMap<>();
	private boolean countingPackets = false;
	
	public ConnectionParameters(Client client) {
		this.client = client;
		
		Arrays.stream(PacketType.values()).forEach(type -> {
			packetIDCounterIncoming.put(type, 1);
			packetIDCounterOutgoing.put(type, 1);
			packetGenerationCounterIncoming.put(type, 0);
			packetGenerationCounterOutgoing.put(type, 0);
		});
	}
	
	protected void beginCountingPackets() {
		packetIDCounterIncoming.put(PacketType.COMMAND, 1);
		packetIDCounterOutgoing.put(PacketType.COMMAND, 0);
		countingPackets = true;
	}
	
	public int incrementPacketCounter(PacketType type, PacketDirection direction) {
		Map<PacketType, Integer> counter 	= direction == SERVER_TO_CLIENT
											? packetIDCounterIncoming : packetIDCounterOutgoing;
		Map<PacketType, Integer> generation	= direction == SERVER_TO_CLIENT
										 	? packetGenerationCounterIncoming : packetGenerationCounterOutgoing;
		
		if (countingPackets) {
			int current = counter.get(type);
			
			if (current >= 65535) {
				System.out.println("Generation counter incremented for packet type " + type.name()); // TODO: debug
				
				counter.put(type, 0);
				generation.put(type, generation.get(type) + 1);
			} else {
				counter.put(type, current + 1);
			}
			
			return counter.get(type);
		} else {
			return -1;
		}
	}
}
