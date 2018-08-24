package pw.lemmmy.ts3protocol.packets.init;

import org.apache.commons.codec.Charsets;
import pw.lemmmy.ts3protocol.commands.CommandClientInitIV;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class PacketInit4 extends PacketInit {
	private byte[] x, n;
	private int level;
	private byte[] serverBytes;
	private CommandClientInitIV initiv;
	
	{
		step = 4;
	}
	
	public PacketInit4(byte[] x, byte[] n, int level, byte[] serverBytes, CommandClientInitIV initiv) {
		this.x = x;
		this.n = n;
		this.level = level;
		this.serverBytes = serverBytes;
		this.initiv = initiv;
	}
	
	@Override
	protected void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		
		os.write(x);
		os.write(n);
		os.writeInt(level);
		
		os.write(serverBytes);
		
		BigInteger y = new BigInteger(1, x).modPow(BigInteger.valueOf(2).pow(level), new BigInteger(1, n));
		byte[] oldBytesY = y.toByteArray();
		byte[] bytesY = new byte[64];
		System.arraycopy(oldBytesY, oldBytesY.length - 64, bytesY, 0, 64);
		os.write(bytesY);
		
		os.write(initiv.encode().getBytes(Charsets.US_ASCII));
		System.out.println(initiv.encode());
	}
}
