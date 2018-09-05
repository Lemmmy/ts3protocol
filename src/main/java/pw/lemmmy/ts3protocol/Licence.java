package pw.lemmmy.ts3protocol;

import lombok.Getter;
import net.i2p.crypto.eddsa.math.GroupElement;
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
	
	private byte[] key;
	
	public void parse(DataInputStream dis) throws IOException {
		dis.readByte(); // version
		
		key = INITIAL_KEY;
		
		try (
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos)
		) {
			while (dis.read() != -1) {
				bos.reset();
				
				byte[] publicKeyBytes = new byte[32];
				dis.read(publicKeyBytes);
				dos.write(publicKeyBytes);
				
				byte blockType = dis.readByte();
				dos.writeByte(blockType);
				dos.writeInt(dis.readInt()); /* date start - epoch */
				dos.writeInt(dis.readInt()); /* date end - epoch */
				
				switch (blockType) {
					case 0: { // Intermediate
						dos.writeInt(dis.readInt());
						readTilNul(dis, dos);
						dos.writeByte(0);
						break;
					}
					case 2: { // Server
						dos.writeByte(dis.readByte());
						dos.writeInt(dis.readInt());
						readTilNul(dis, dos);
						dos.writeByte(0);
						break;
					}
					case 32: { // Ephemeral
						break;
					}
				}
				
				dos.flush();
				
				byte[] blockData = bos.toByteArray();
				byte[] hashedBlock = CryptoUtils.sha512(blockData);
				byte[] hashOut = new byte[32];
				System.arraycopy(hashedBlock, 0, hashOut, 0, 32);
				
				// scalar clamp the hash
				hashOut[0]  &= 0xF8;
				hashOut[31] &= 0x3F;
				hashOut[31] |= 0x40;
				
				GroupElement publicKey = CryptoUtils.decompressEdPoint(publicKeyBytes);
				GroupElement parentKey = CryptoUtils.decompressEdPoint(key);
				
				key = publicKey.scalarMultiply(hashOut).toP3().add(parentKey.toCached()).toP3().toByteArray();
			}
		}
	}
	
	private void readTilNul(DataInputStream dis, DataOutputStream dos) throws IOException {
		for (int b; (b = dis.read()) > 0;) dos.writeByte(b);
	}
}
