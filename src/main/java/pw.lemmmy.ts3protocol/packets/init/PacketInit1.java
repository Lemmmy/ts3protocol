package pw.lemmmy.ts3protocol.packets.init;

import pw.lemmmy.ts3protocol.packets.PacketDirection;

import java.io.DataInputStream;
import java.io.IOException;

public class PacketInit1 extends PacketInit {
	private byte[] randomBytes, serverBytes;
	
	{
		direction = PacketDirection.SERVER_TO_CLIENT;
	}
	
	public PacketInit1(byte[] randomBytes, byte[] serverBytes) {
		this.randomBytes = randomBytes;
		this.serverBytes = serverBytes;
	}
	
	@Override
	protected void readData(DataInputStream is, int length) throws IOException {
		super.readData(is, length);
		
		is.read(serverBytes);
		is.read(randomBytes); // the old randomBytes, but in reversed order
	}
}
