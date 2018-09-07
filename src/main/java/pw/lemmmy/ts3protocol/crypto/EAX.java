package pw.lemmmy.ts3protocol.crypto;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import static pw.lemmmy.ts3protocol.packets.LowLevelPacket.MAC_SIZE;

public class EAX {
	public static byte[][] eaxEncrypt(byte[] key, byte[] nonce, byte[] header, byte[] data) throws
																							InvalidCipherTextException {
		EAXBlockCipher cipher = new EAXBlockCipher(new AESEngine());
		cipher.init(true, new AEADParameters(new KeyParameter(key), MAC_SIZE * 8, nonce, header));
		
		byte[] rawEnc = new byte[cipher.getOutputSize(data.length)];
		int len = cipher.processBytes(data, 0, data.length, rawEnc, 0);
		cipher.doFinal(rawEnc, len);
		
		// rawEnc includes the MAC, lets strip that out)
		// TODO: find a better way
		byte[] enc = new byte[rawEnc.length - MAC_SIZE];
		System.arraycopy(rawEnc, 0, enc, 0, enc.length);
		
		return new byte[][] { cipher.getMac(), enc };
	}
	
	public static byte[] eaxDecrypt(byte[] key, byte[] nonce, byte[] header, byte[] data, byte[] mac) throws InvalidCipherTextException {
		EAXBlockCipher cipher = new EAXBlockCipher(new AESEngine());
		cipher.init(false, new AEADParameters(new KeyParameter(key), mac.length * 8, nonce, header));
		
		// concatenate the data and mac into one input array
		byte[] in = new byte[data.length + mac.length];
		System.arraycopy(data, 0, in, 0, data.length);
		System.arraycopy(mac, 0, in, data.length, mac.length);
		
		byte[] dec = new byte[cipher.getOutputSize(in.length)];
		
		int len = cipher.processBytes(in, 0, in.length, dec, 0);
		cipher.doFinal(dec, len);
		
		return dec;
	}
}
