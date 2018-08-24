package pw.lemmmy.ts3protocol.packets.init;

import pw.lemmmy.ts3protocol.packets.LowLevelPacket;
import pw.lemmmy.ts3protocol.packets.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class PacketInit extends LowLevelPacket {
	byte step = 0;
	
	public PacketInit() {
		this.mac = LowLevelPacket.HANDSHAKE_MAC;
		this.packetType = PacketType.INIT_1;
		this.packetID = 0x65;
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
