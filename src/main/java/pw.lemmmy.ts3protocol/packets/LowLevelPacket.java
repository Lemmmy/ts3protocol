package pw.lemmmy.ts3protocol.packets;

import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;

@Getter
@Setter
public class LowLevelPacket {
	public static final int PACKET_SIZE = 500;
	public static final byte[] HANDSHAKE_MAC = { 0x54, 0x53, 0x33, 0x49, 0x4E, 0x49, 0x54, 0x31 };
	
	protected byte[] mac;
	protected short packetID, clientID;
	protected PacketType packetType;
	protected boolean unencrypted = true, compressed, newProtocol, fragmented, midFragmented;
	protected byte[] data;
	
	public void writePacketByte(DataOutputStream os) throws IOException {
		BitSet pt = BitSet.valueOf(new byte[]{(byte) packetType.ordinal()});
		
		if (!midFragmented) {
			pt.set(7, unencrypted);
			pt.set(6, compressed);
			pt.set(5, newProtocol);
			pt.set(4, fragmented);
		}
		
		os.write(pt.toByteArray());
	}
	
	public void writeMeta(DataOutputStream os) throws IOException {
		/* Packet ID */ os.writeShort(packetID);
		/* Client ID */ os.writeShort(clientID);
		
		/* Packet Type + Flags */
		writePacketByte(os);
	}
	
	public void write(DataOutputStream os) throws IOException {
		os.write(mac);
		writeMeta(os);
		writeData(os);
	}
	
	protected void writeData(DataOutputStream os) throws IOException {
		os.write(data);
	}
	
	public void read(DataInputStream is) throws IOException {
		/* MAC */
		mac = new byte[8];
		is.read(mac);
		
		/* Packet ID */ packetID = is.readShort();
		
		/* Packet Type + Flags */
		byte packetTypeByte = is.readByte();
		
		BitSet pt = BitSet.valueOf(new byte[] { packetTypeByte });
		unencrypted = pt.get(7);
		compressed  = pt.get(6);
		newProtocol = pt.get(5);
		fragmented  = pt.get(4);
		
		packetType = PacketType.values()[packetTypeByte & 0xF];
		
		/* Data */
		readData(is, PACKET_SIZE - 11);
	}
	
	protected void readData(DataInputStream is, int length) throws IOException {
		is.readFully(data, 0, length);
	}
}
