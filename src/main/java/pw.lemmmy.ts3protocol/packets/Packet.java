package pw.lemmmy.ts3protocol.packets;

import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.crypto.InvalidCipherTextException;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;
import pw.lemmmy.ts3protocol.utils.QuickLZ;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Getter
@Setter
public class Packet {
	private static final int FRAGMENTED_DATA_SIZE = LowLevelPacket.PACKET_SIZE - 13;
	
	protected byte[][] macs;
	protected short[] packetIDs, clientIDs;
	protected PacketType packetType;
	protected boolean unencrypted = true, compressed, newProtocol;
	protected byte[] data;
	
	public void read(LowLevelPacket[] packets) throws IOException {
		macs = new byte[packets.length][];
		packetIDs = new short[packets.length];
		clientIDs = new short[packets.length];
		
		unencrypted = packets[0].unencrypted;
		compressed = packets[0].compressed;
		newProtocol = packets[0].compressed;
		
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			for (int i = 0; i < packets.length; i++) {
				LowLevelPacket packet = packets[i];
				
				macs[i] = packet.mac;
				packetIDs[i] = packet.packetID;
				clientIDs[i] = packet.clientID;
				
				// TODO: generation counters
				
				if (unencrypted) {
					bos.write(packet.data);
				} else {
				
				}
			}
			data = bos.toByteArray();
		}
		
		if (compressed) decompress();
		
		// TODO: deal with encryption and compression
	}
	
	protected void writeData(DataOutputStream os) throws IOException {}
	
	public LowLevelPacket[] write() {
		try (
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos)
		) {
			writeData(dos);
			dos.flush();
			data = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] compressedData = compressed ? QuickLZ.compress(data, 1) : data;
		
		int packetCount = (int) Math.ceil((float) compressedData.length / (float) FRAGMENTED_DATA_SIZE);
		boolean fragmented = packetCount > 1;
		LowLevelPacket[] packets = new LowLevelPacket[packetCount];
		
		for (int i = 0; i < packetCount; i++) {
			LowLevelPacket packet = new LowLevelPacket();
			
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
			
			packet.data = new byte[FRAGMENTED_DATA_SIZE];
			System.arraycopy(
				compressedData, i * FRAGMENTED_DATA_SIZE,
				packet.data, 0,
				i == packetCount - 1 ? compressedData.length % FRAGMENTED_DATA_SIZE : FRAGMENTED_DATA_SIZE
			);
			
			if (!unencrypted) {
				// TODO: non-fake encrypt after IV stuff
				try (
					ByteArrayOutputStream headerBOS = new ByteArrayOutputStream(5);
					DataOutputStream headerDOS = new DataOutputStream(headerBOS);
					ByteArrayOutputStream dataBOS = new ByteArrayOutputStream(FRAGMENTED_DATA_SIZE);
					DataOutputStream dataDOS = new DataOutputStream(dataBOS)
				) {
					packet.writeMeta(headerDOS);
					packet.writeData(dataDOS);
					
					headerDOS.flush();
					dataDOS.flush();
					
					byte[][] encrypted = CryptoUtils.eaxEncrypt(
						CryptoUtils.FAKE_EAX_KEY,
						CryptoUtils.FAKE_EAX_NONCE,
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
	
	private void decompress() {
		data = QuickLZ.decompress(data);
	}
}
