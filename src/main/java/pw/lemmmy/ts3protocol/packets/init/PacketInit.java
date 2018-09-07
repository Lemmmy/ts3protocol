package pw.lemmmy.ts3protocol.packets.init;

import pw.lemmmy.ts3protocol.crypto.Crypto;
import pw.lemmmy.ts3protocol.packets.LowLevelPacket;
import pw.lemmmy.ts3protocol.packets.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class PacketInit extends LowLevelPacket {
	public static final short INIT1_PACKET_ID = 0x65;
	
	byte step = 0;
	
	{
		newProtocol = true;
		mac = Crypto.HANDSHAKE_MAC;
		packetType = PacketType.INIT_1;
		packetID = INIT1_PACKET_ID;
	}
	
	@Override
	protected void writeData(DataOutputStream os) throws IOException {
		long unixTime = System.currentTimeMillis() / 1000L;
		
		os.writeInt((int) unixTime);
		os.writeByte(step); // init-packet step number
	}
	
	@Override
	protected void readData(DataInputStream is, int length) throws IOException {
		step = is.readByte();
	}
}
