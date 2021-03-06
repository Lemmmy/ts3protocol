package pw.lemmmy.ts3protocol.packets.init;

import pw.lemmmy.ts3protocol.packets.PacketDirection;

import java.io.DataOutputStream;
import java.io.IOException;

public class PacketInit0 extends PacketInit {
	private byte[] randomBytes;
	
	{
		step = 0;
		direction = PacketDirection.CLIENT_TO_SERVER;
	}
	
	public PacketInit0(byte[] randomBytes) {
		this.randomBytes = randomBytes;
	}
	
	@Override
	protected void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		
		long unixTime = System.currentTimeMillis() / 1000L;
		os.writeInt((int) unixTime);
		
		os.write(randomBytes);
		os.writeLong(0);
	}
}
