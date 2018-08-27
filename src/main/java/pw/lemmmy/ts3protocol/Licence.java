package pw.lemmmy.ts3protocol;

import lombok.Getter;
import net.i2p.crypto.eddsa.math.GroupElement;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.util.encoders.Hex;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Getter
public class Licence {
	private static final int LICENCE_EPOCH = 0x50e22700;
	
	private static final byte[] INITIAL_KEY = {
		(byte) 0xCD, (byte) 0x0D, (byte) 0xE2, (byte) 0xAE, (byte) 0xD4, (byte) 0x63, (byte) 0x45, (byte) 0x50,
		(byte) 0x9A, (byte) 0x7E, (byte) 0x3C, (byte) 0xFD, (byte) 0x8F, (byte) 0x68, (byte) 0xB3, (byte) 0xDC,
		(byte) 0x75, (byte) 0x55, (byte) 0xB2, (byte) 0x9D, (byte) 0xCC, (byte) 0xEC, (byte) 0x73, (byte) 0xCD,
		(byte) 0x18, (byte) 0x75, (byte) 0x0F, (byte) 0x99, (byte) 0x38, (byte) 0x12, (byte) 0x40, (byte) 0x8A
	};
	
	private GroupElement key;
	
	public void parse(DataInputStream dis) throws IOException {
		dis.readByte(); // version
		
		key = CryptoUtils.decompressEdPoint(INITIAL_KEY);
		
		try (
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos)
		) {
			while (dis.read() != -1) {
				System.out.println("Reading block");
				bos.reset();
				
				byte[] publicKeyBytes = new byte[32];
				dis.read(publicKeyBytes);
				dos.write(publicKeyBytes);
				
				System.out.println("	Block pubkey: " + Hex.toHexString(publicKeyBytes));
				
				byte blockType = dis.readByte();
				dos.writeByte(blockType);
				dos.write(dis.readInt()); /* date start - epoch */
				dos.write(dis.readInt()); /* date end - epoch */
				
				switch (blockType) {
					case 0x00: { // Intermediate
						System.out.println("	Intermediate");
						dos.writeInt(dis.readInt());
						readTilNul(dis, dos);
						break;
					}
					case 0x01: { // Website
						System.out.println("	Website");
						readTilNul(dis, dos);
						break;
					}
					case 0x02: { // Server
						System.out.println("	Server");
						dos.writeByte(dis.readByte());
						dos.writeInt(dis.readInt());
						readTilNul(dis, dos);
						break;
					}
					case 0x03: { // Code
						System.out.println("	Code");
						readTilNul(dis, dos);
						break;
					}
					case 0x32: { // Ephemeral
						System.out.println("	Ephemeral");
						break;
					}
				}
				
				dos.flush();
				
				byte[] blockData = bos.toByteArray();
				byte[] hashedBlock = CryptoUtils.sha512(blockData);
				byte[] hashOut = new byte[32];
				System.arraycopy(hashedBlock, 0, hashOut, 0, 32);
				
				System.out.println("	Hashed block (" + hashedBlock.length + "): " + Hex.toHexString(hashedBlock));
				System.out.println("	Hashed block (short): " + Hex.toHexString(hashOut));
				
				GroupElement publicKey = CryptoUtils.decompressEdPoint(publicKeyBytes);
				System.out.println("	Key: " + publicKey.toString());
				
				hashOut[0]  &= 0xF8;
				hashOut[31] &= 0x3F;
				hashOut[31] |= 0x40;
				
				System.out.println("	After manipulation:");
				System.out.println("	Hashed block (short): " + Hex.toHexString(hashOut));
				
				key = new GroupElement(
					key.getCurve(),
					publicKey
						.scalarMultiply(hashOut)
						.add(key.toP3().toCached())
						.toByteArray(),
					true
				);
				System.out.println("	Final key: " + key.toString());
			}
		}
	}
	
	private void readTilNul(DataInputStream dis, DataOutputStream dos) throws IOException {
		for (int b; (b = dis.read()) > 0;) dos.writeByte(b);
	}
}
