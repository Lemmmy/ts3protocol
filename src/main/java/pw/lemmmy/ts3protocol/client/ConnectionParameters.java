package pw.lemmmy.ts3protocol.client;

import lombok.Getter;
import lombok.Setter;
import pw.lemmmy.ts3protocol.crypto.CachedKey;
import pw.lemmmy.ts3protocol.crypto.Crypto;
import pw.lemmmy.ts3protocol.packets.PacketType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ConnectionParameters {
	private Client client;
	
	// temporary key/nonce until the handshake is complete
	private byte[] eaxKey = Crypto.FAKE_EAX_KEY;
	private byte[] eaxNonce = Crypto.FAKE_EAX_NONCE;
	
	@Setter private byte[] sharedIV;
	@Setter private byte[] sharedMac;
	
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
	
	public int setPacketCounter(int value, PacketType type) {
		Map<PacketType, Integer> counter 	= packetIDCounterIncoming;
		Map<PacketType, Integer> generation	= packetGenerationCounterIncoming;
		
		if (countingPackets) {
			int current = counter.get(type);
			counter.put(type, value);
			
			if (current == 65535 && value == 0) {
				generation.put(type, generation.get(type) + 1);
			}
			
			return counter.get(type);
		} else {
			return -1;
		}
	}
	
	public int incrementPacketCounter(PacketType type) {
		Map<PacketType, Integer> counter 	= packetIDCounterOutgoing;
		Map<PacketType, Integer> generation	= packetGenerationCounterOutgoing;
		
		if (countingPackets) {
			int current = counter.get(type);
			
			if (current >= 65535) {
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
