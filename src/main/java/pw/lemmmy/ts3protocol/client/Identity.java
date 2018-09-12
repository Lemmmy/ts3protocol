package pw.lemmmy.ts3protocol.client;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import pw.lemmmy.ts3protocol.crypto.EC;
import pw.lemmmy.ts3protocol.crypto.HashCash;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Getter
@Accessors(chain = true)
public class Identity {
	private static final byte DEFAULT_LEVEL = 8;
	
	@Setter private String nickname = "TeamSpeakUser";
	@Setter private String phoneticNickname = nickname;
	@Setter private String defaultChannel = "", defaultChannelPassword = "";
	
	private KeyPair keyPair;
	private byte securityLevel;
	private long keyOffset = -1;
	
	public Identity() {
		this(DEFAULT_LEVEL);
	}
	
	public Identity(byte securityLevel) {
		this.securityLevel = securityLevel;
		
		generateKeyPair();
	}
	
	public Identity(File publicKeyFile, File privateKeyFile) {
		this(DEFAULT_LEVEL, publicKeyFile, privateKeyFile);
	}
	
	public Identity(byte securityLevel, File publicKeyFile, File privateKeyFile) {
		this.securityLevel = securityLevel;
		
		if (!publicKeyFile.exists() || !privateKeyFile.exists()) {
			generateKeyPair();
			writeKeyPair(publicKeyFile, privateKeyFile);
		} else {
			readKeyPair(publicKeyFile, privateKeyFile);
		}
	}
	
	private void generateKeyPair() {
		try {
			keyPair = EC.generateECDHKeypair();
			keyOffset = HashCash.hashCash(keyPair, securityLevel);
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeKeyPair(File publicKey, File privateKey) {
		try {
			Files.write(publicKey.toPath(), new X509EncodedKeySpec(keyPair.getPublic().getEncoded()).getEncoded());
			Files.write(privateKey.toPath(), new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()).getEncoded());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readKeyPair(File publicKey, File privateKey) {
		try {
			keyPair = EC.decodeECDHKeypair(
				Files.readAllBytes(publicKey.toPath()),
				Files.readAllBytes(privateKey.toPath())
			);
			keyOffset = HashCash.hashCash(keyPair, securityLevel);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			e.printStackTrace();
		}
	}
}
