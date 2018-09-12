package pw.lemmmy.ts3protocol.client;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import pw.lemmmy.ts3protocol.crypto.EC;
import pw.lemmmy.ts3protocol.crypto.HashCash;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

@Getter
@Accessors(chain = true)
public class Identity {
	private static final byte DEFAULT_LEVEL = 8;
	
	@Setter private String nickname = "TeamSpeakUser";
	@Setter private String phoneticNickname = nickname;
	
	@Setter private short defaultChannelID = -1;
	
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
	
	private void generateKeyPair() {
		try {
			// TODO: persist the keypair for a consistent identity
			keyPair = EC.generateECDHKeypair();
			keyOffset = HashCash.hashCash(keyPair, securityLevel);
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e) {
			e.printStackTrace();
		}
	}
}
