package pw.lemmmy.ts3protocol.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
	public static byte[] hashDigest(String algorithm, byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm, Crypto.PROVIDER);
			return md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return data;
		}
	}
	
	public static byte[] sha1(byte[] data) {
		return hashDigest("SHA-1", data);
	}
	
	public static byte[] sha256(byte[] data) {
		return hashDigest("SHA-256", data);
	}
	
	public static byte[] sha512(byte[] data) {
		return hashDigest("SHA-512", data);
	}
}
