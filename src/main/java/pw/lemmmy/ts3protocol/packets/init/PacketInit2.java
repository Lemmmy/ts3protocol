package pw.lemmmy.ts3protocol.packets.init;

import pw.lemmmy.ts3protocol.packets.PacketDirection;

import java.io.DataOutputStream;
import java.io.IOException;

public class PacketInit2 extends PacketInit {
	private byte[] randomBytes, serverBytes;
	
	{
		step = 2;
		direction = PacketDirection.CLIENT_TO_SERVER;
	}
	
	public PacketInit2(byte[] randomBytes, byte[] serverBytes) {
		this.randomBytes = randomBytes;
		this.serverBytes = serverBytes;
	}
	
	@Override
	protected void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		
		os.write(serverBytes);
		os.write(randomBytes);
	}
}
