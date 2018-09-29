package pw.lemmmy.ts3protocol.crypto;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class Hash {
	public static byte[] hashDigest(String algorithm, byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm, Crypto.PROVIDER);
			return md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			log.error("Provider doesn't provide hash algorithm {}", algorithm, e);
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
