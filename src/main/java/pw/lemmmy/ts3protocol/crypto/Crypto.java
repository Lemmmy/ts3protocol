package pw.lemmmy.ts3protocol.crypto;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.util.encoders.Base64;

import java.security.Security;

public class Crypto {
	public static final byte[] FAKE_EAX_KEY = { 0x63, 0x3A, 0x5C, 0x77, 0x69, 0x6E, 0x64, 0x6F, 0x77,
		0x73, 0x5C, 0x73, 0x79, 0x73, 0x74, 0x65 };
	
	public static final byte[] FAKE_EAX_NONCE = { 0x6D, 0x5C, 0x66, 0x69, 0x72, 0x65, 0x77, 0x61, 0x6C,
		0x6C, 0x33, 0x32, 0x2E, 0x63, 0x70, 0x6C };
	
	public static final byte[] HANDSHAKE_MAC = { 0x54, 0x53, 0x33, 0x49, 0x4E, 0x49, 0x54, 0x31 };
	
	public static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();
	
	public static final ECNamedCurveParameterSpec PRIME256_V1 = ECNamedCurveTable.getParameterSpec("prime256v1");
	
	static {
		Security.addProvider(PROVIDER);
	}
	
	public static String hashTeamspeakPassword(String password) {
		return password == null || password.isEmpty() ? "" : Base64.toBase64String(Hash.sha1(password.getBytes()));
	}
}
