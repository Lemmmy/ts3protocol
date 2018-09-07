package pw.lemmmy.ts3protocol.crypto;

import java.io.IOException;
import java.security.KeyPair;

public class HashCash {
	public static long hashCash(KeyPair keyPair, byte level) throws IOException {
		String omega = ASN.encodeBase64ASN(keyPair);
		
		long offset = 0L;
		while (offset < Long.MAX_VALUE && getHashCashLevel(omega, offset) < level) {
			offset += 1;
		}
		
		return offset;
	}
	
	private static int getLeadingZeros(byte data) {
		for (int i = 0; i < 8; i++) {
			if ((data & (1 << 7)) != 0) {
				return i;
			}
			
			data <<= 1;
		}
		
		return 8;
	}
	
	private static byte getHashCashLevel(String omega, long offset) {
		byte[] data = Hash.sha1((omega + Long.toString(offset)).getBytes());
		byte res = 0;
		
		for (byte d : data) {
			if (d == 0) {
				res += 8;
			} else {
				res += getLeadingZeros(d);
				break;
			}
		}
		
		return res;
	}
}
