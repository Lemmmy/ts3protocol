package pw.lemmmy.ts3protocol.packets.init;

import lombok.Getter;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;

@Getter
public class PacketInit3 extends PacketInit {
	private byte[] serverBytes;
	
	private byte[] x = new byte[64], n = new byte[64];
	private int level;
	
	public PacketInit3(byte[] serverBytes) {
		this.serverBytes = serverBytes;
	}
	
	@Override
	protected void readData(DataInputStream is, int length) throws IOException {
		super.readData(is, length);
		
		is.read(x);
		is.read(n);
		
		level = is.readInt();
		
		is.read(serverBytes);
	}
}
