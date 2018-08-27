package pw.lemmmy.ts3protocol.packets;

import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Hex;
import pw.lemmmy.ts3protocol.Client;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;
import pw.lemmmy.ts3protocol.utils.QuickLZ;

import java.io.*;

import static pw.lemmmy.ts3protocol.packets.PacketDirection.CLIENT_TO_SERVER;
import static pw.lemmmy.ts3protocol.packets.PacketDirection.SERVER_TO_CLIENT;

@Getter
@Setter
public class Packet {
	protected PacketDirection direction;
	protected byte[][] macs;
	protected short[] packetIDs, clientIDs;
	protected PacketType packetType;
	protected boolean unencrypted = true, compressed, newProtocol;
	protected byte[] data;
	
	public void read(Client client, LowLevelPacket[] packets) throws IOException {
		macs = new byte[packets.length][];
		packetIDs = new short[packets.length];
		
		unencrypted = packets[0].unencrypted;
		compressed = packets[0].compressed;
		newProtocol = packets[0].compressed;
		
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			for (int i = 0; i < packets.length; i++) {
				LowLevelPacket packet = packets[i];
				
				macs[i] = packet.mac;
				packetIDs[i] = packet.packetID;
				
				// TODO: generation counters
				
				if (unencrypted) {
					bos.write(packet.data);
				} else {
					// TODO: non-fake decrypt after IV stuff
					try (
						ByteArrayOutputStream headerBOS = new ByteArrayOutputStream(SERVER_TO_CLIENT.getMetaSize());
						DataOutputStream headerDOS = new DataOutputStream(headerBOS)
					) {
						packet.writeMeta(headerDOS);
						headerDOS.flush();
						
						byte[] decrypted = CryptoUtils.eaxDecrypt(
							client.getEaxKey(),
							client.getEaxNonce(),
							headerBOS.toByteArray(),
							packet.data,
							packet.mac
						);
						
						System.out.println("decrypted: " + Hex.toHexString(decrypted));
						
						bos.write(decrypted);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			bos.flush();
			data = bos.toByteArray();
		}
		
		if (compressed) data = QuickLZ.decompress(data);
		
		try (
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis)
		) {
			readData(client, dis);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void readData(Client client, DataInputStream dis) throws IOException {}
	
	public LowLevelPacket[] write(Client client) {
		try (
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos)
		) {
			writeData(client, dos);
			dos.flush();
			data = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] compressedData = compressed ? QuickLZ.compress(data, 1) : data;
		
		int fragmentedDataSize = LowLevelPacket.PACKET_SIZE - CLIENT_TO_SERVER.getHeaderSize();
		int packetCount = (int) Math.ceil((float) compressedData.length / (float) fragmentedDataSize);
		boolean fragmented = packetCount > 1;
		LowLevelPacket[] packets = new LowLevelPacket[packetCount];
		
		for (int i = 0; i < packetCount; i++) {
			LowLevelPacket packet = new LowLevelPacket();
			
			int id = client.incrementPacketCounter(packetType, client.getPacketIDCounterOutgoing(), client.getPacketGenerationCounterOutgoing());
			if (id != -1) packet.setPacketID((short) id);
			
			if (i == 0) {
				packet.packetType = packetType;
				packet.unencrypted = unencrypted;
				packet.compressed = compressed;
				packet.newProtocol = newProtocol;
				packet.fragmented = fragmented;
			} else if (i == packetCount - 1) {
				packet.fragmented = true;
			} else {
				packet.midFragmented = true;
			}
			
			packet.data = new byte[fragmentedDataSize];
			System.arraycopy(
				compressedData, i * fragmentedDataSize,
				packet.data, 0,
				i == packetCount - 1 ? compressedData.length % fragmentedDataSize : fragmentedDataSize
			);
			
			if (!unencrypted) {
				// TODO: non-fake encrypt after IV stuff
				try (
					ByteArrayOutputStream headerBOS = new ByteArrayOutputStream(CLIENT_TO_SERVER.getMetaSize());
					DataOutputStream headerDOS = new DataOutputStream(headerBOS);
					ByteArrayOutputStream dataBOS = new ByteArrayOutputStream(fragmentedDataSize);
					DataOutputStream dataDOS = new DataOutputStream(dataBOS)
				) {
					packet.writeMeta(headerDOS);
					packet.writeData(dataDOS);
					
					headerDOS.flush();
					dataDOS.flush();
					
					byte[][] encrypted = CryptoUtils.eaxEncrypt(
						client.getEaxKey(),
						client.getEaxNonce(),
						headerBOS.toByteArray(),
						dataBOS.toByteArray()
					);
					
					packet.mac = encrypted[0];
					packet.data = encrypted[1];
				} catch (IOException | InvalidCipherTextException e) {
					e.printStackTrace();
				}
			}
			
			packets[i] = packet;
		}
		
		return packets;
	}
	
	protected void writeData(Client client, DataOutputStream os) throws IOException {}
}
