package pw.lemmmy.ts3protocol.packets;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.crypto.InvalidCipherTextException;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.client.ConnectionParameters;
import pw.lemmmy.ts3protocol.crypto.CachedKey;
import pw.lemmmy.ts3protocol.crypto.EAX;
import pw.lemmmy.ts3protocol.crypto.Hash;
import pw.lemmmy.ts3protocol.utils.QuickLZ;

import java.io.*;

import static pw.lemmmy.ts3protocol.packets.PacketDirection.CLIENT_TO_SERVER;
import static pw.lemmmy.ts3protocol.packets.PacketDirection.SERVER_TO_CLIENT;

@Getter
@Setter
@Slf4j
public class Packet {
	protected PacketDirection direction;
	protected byte[][] macs;
	protected int[] packetIDs;
	protected PacketType packetType;
	protected boolean unencrypted = true, compressed, newProtocol;
	protected byte[] data;
	
	public void read(Client client, LowLevelPacket[] packets) throws IOException {
		ConnectionParameters params = client.params;
		
		macs = new byte[packets.length][];
		packetIDs = new int[packets.length];
		
		unencrypted = packets[0].unencrypted;
		compressed = packets[0].compressed;
		newProtocol = packets[0].compressed;
		
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			for (int i = 0; i < packets.length; i++) {
				LowLevelPacket packet = packets[i];
				
				macs[i] = packet.mac;
				packetIDs[i] = packet.packetID;
				
				int generationID = params.getPacketGenerationCounterIncoming().get(packetType);
				
				if (unencrypted) {
					bos.write(packet.data);
				} else {
					try (
						ByteArrayOutputStream headerBOS = new ByteArrayOutputStream(SERVER_TO_CLIENT.getMetaSize());
						DataOutputStream headerDOS = new DataOutputStream(headerBOS)
					) {
						byte[][] keyNonce =
							client.packetHandler.shouldEncryptPackets() ? createKeyNonce(client, packet.packetID, generationID) : null;
						
						packet.writeMeta(headerDOS);
						headerDOS.flush();
						
						try {
							byte[] decrypted = EAX.eaxDecrypt(
								keyNonce != null ? keyNonce[0] : params.getEaxKey(),
								keyNonce != null ? keyNonce[1] : params.getEaxNonce(),
								headerBOS.toByteArray(),
								packet.data,
								packet.mac
							);
							
							bos.write(decrypted);
						} catch (InvalidCipherTextException e) {
							if (packetType != PacketType.ACK) { // normal for the first ACK
								log.error("Failed to decrypt data with calculated key, trying shared key");
							}
							
							try {
								byte[] decrypted = EAX.eaxDecrypt(
									params.getEaxKey(),
									params.getEaxNonce(),
									headerBOS.toByteArray(),
									packet.data,
									packet.mac
								);
								
								bos.write(decrypted);
							} catch (InvalidCipherTextException e1) {
								log.error("Can't decrypt data (mac check failed) with calculated and shared keys", e);
							}
						}
					} catch (IOException e) {
						log.error("Error reading high-level packet", e);
					}
				}
			}
			bos.flush();
			data = bos.toByteArray();
		}
		
		if (compressed && packetType.isCompressible()) data = QuickLZ.decompress(data);
		
		try (
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis)
		) {
			readData(client, dis);
		} catch (IOException e) {
			log.error("Error reading data from high-level packet", e);
		}
	}
	
	protected void readData(Client client, DataInputStream dis) throws IOException {}
	
	public LowLevelPacket[] write(Client client) {
		ConnectionParameters params = client.params;
		boolean shouldEncrypt = client.packetHandler.shouldEncryptPackets();
		
		try (
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos)
		) {
			writeData(client, dos);
			dos.flush();
			data = bos.toByteArray();
		} catch (IOException e) {
			log.error("Error writing data to high-level packet", e);
		}
		
		byte[] compressedData = compressed && packetType.isCompressible() ? QuickLZ.compress(data, 1) : data;
		
		int fragmentedDataSize = LowLevelPacket.PACKET_SIZE - CLIENT_TO_SERVER.getHeaderSize();
		int packetCount = (int) Math.ceil((double) compressedData.length / (double) fragmentedDataSize);
		boolean fragmented = packetCount > 1;
		LowLevelPacket[] packets = new LowLevelPacket[packetCount];
		
		for (int i = 0; i < packetCount; i++) {
			LowLevelPacket packet = new LowLevelPacket();
			packet.setClientID(client.getID());
			packet.setDirection(direction);
			int packetSize = i == packetCount - 1 ? compressedData.length % fragmentedDataSize : fragmentedDataSize;
			
			int id = params.incrementPacketCounter(packetType);
			int generationID = params.getPacketGenerationCounterOutgoing().get(packetType);
			if (id != -1) packet.setPacketID(id);
			
			packet.packetType = packetType;
			
			if (i == 0) {
				packet.unencrypted = unencrypted;
				packet.compressed = compressed;
				packet.newProtocol = newProtocol;
				packet.fragmented = fragmented;
			} else if (i == packetCount - 1) {
				packet.fragmented = true;
			} else {
				packet.midFragmented = true;
			}
			
			packet.data = new byte[packetSize];
			System.arraycopy(
				compressedData, i * fragmentedDataSize,
				packet.data, 0,
				packetSize
			);
			
			if (unencrypted && shouldEncrypt) {
				packet.mac = params.getSharedMac();
			} else if (!unencrypted) {
				try (
					ByteArrayOutputStream headerBOS = new ByteArrayOutputStream(CLIENT_TO_SERVER.getMetaSize());
					DataOutputStream headerDOS = new DataOutputStream(headerBOS)
				) {
					byte[][] keyNonce = shouldEncrypt ? createKeyNonce(client, id, generationID) : null;
					
					packet.writeMeta(headerDOS);
					
					headerDOS.flush();
					
					byte[][] encrypted = EAX.eaxEncrypt(
						keyNonce != null ? keyNonce[0] : params.getEaxKey(),
						keyNonce != null ? keyNonce[1] : params.getEaxNonce(),
						headerBOS.toByteArray(),
						packet.data
					);
					
					packet.mac = encrypted[0];
					packet.data = encrypted[1];
				} catch (IOException | InvalidCipherTextException e) {
					log.error("Error writing high-level packet", e);
				}
			}
			
			packets[i] = packet;
		}
		
		return packets;
	}
	
	protected void writeData(Client client, DataOutputStream os) throws IOException {}
	
	private byte[][] createKeyNonce(Client client, int packetID, int generationID) {
		ConnectionParameters params = client.params;
		val keyCache = direction == SERVER_TO_CLIENT ? params.getKeyCacheIncoming() : params.getKeyCacheOutgoing();
		
		if (!keyCache.containsKey(packetType) || keyCache.get(packetType).getGenerationID() != generationID) {
			try (
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos)
			) {
				dos.writeByte(direction == SERVER_TO_CLIENT ? 0x30 : 0x31);
				dos.writeByte(packetType.ordinal() & 0xF);
				dos.writeInt(generationID);
				dos.write(params.getSharedIV());
				
				dos.flush();
				
				byte[] data = bos.toByteArray();
				
				byte[] keyNonce = Hash.sha256(data);
				byte[] key = new byte[16], nonce = new byte[16];
				
				System.arraycopy(keyNonce, 0, key, 0, 16);
				System.arraycopy(keyNonce, 16, nonce, 0, 16);
				
				keyCache.put(packetType, new CachedKey(generationID, key, nonce));
			} catch (IOException e) {
				log.error("Error creating EAX key-nonce pair for packet {} (id: {} gen: {})", packetType.name(), packetID, generationID, e);
			}
		}
		
		CachedKey cachedKey = keyCache.get(packetType);
		byte[] cachedKeyKey = cachedKey.getKey();
		byte[] key = new byte[cachedKeyKey.length];
		System.arraycopy(cachedKeyKey, 0, key, 0, key.length);
		key[0] ^= packetID >> 8;
		key[1] ^= packetID & 0xFF;
		
		return new byte[][] { key, cachedKey.getNonce() };
	}
}
