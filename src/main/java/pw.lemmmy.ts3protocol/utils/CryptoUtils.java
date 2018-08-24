package pw.lemmmy.ts3protocol.utils;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.*;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;

public class CryptoUtils {
	public static final byte[] FAKE_EAX_KEY = { 0x63, 0x3A, 0x5C, 0x77, 0x69, 0x6E, 0x64, 0x6F, 0x77,
		0x73, 0x5C, 0x73, 0x79, 0x73, 0x74, 0x65 };
	
	public static final byte[] FAKE_EAX_NONCE = { 0x6D, 0x5C, 0x66, 0x69, 0x72, 0x65, 0x77, 0x61, 0x6C,
		0x6C, 0x33, 0x32, 0x2E, 0x63, 0x70, 0x6C };
	
	public static final ECNamedCurveParameterSpec PRIME256_V1 = ECNamedCurveTable.getParameterSpec("prime256v1");
	public static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();
	
	// TODO: key persistence
	public static KeyPair generateECDHKeypair() throws NoSuchAlgorithmException,
													   InvalidAlgorithmParameterException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDH", PROVIDER);
		generator.initialize(PRIME256_V1, new SecureRandom());
		KeyPair keyPair = generator.generateKeyPair();
		
		System.out.println("Pub: " + Hex.encodeHexString(keyPair.getPublic().getEncoded()));
		System.out.println("Priv: " + Hex.encodeHexString(keyPair.getPrivate().getEncoded()));
		
		return keyPair;
	}
	
	public static DERSequence toTomcrypt(KeyPair kp) {
		ECPublicKey pubKey = (ECPublicKey) kp.getPublic();
		
		BigInteger x = pubKey.getQ().getAffineXCoord().toBigInteger();
		BigInteger y = pubKey.getQ().getAffineYCoord().toBigInteger();
		
		return new DERSequence(new ASN1Encodable[] {
			new DERBitString(0),
			new ASN1Integer((short) 32),
			new ASN1Integer(x),
			new ASN1Integer(y)
		});
	}
	
	public static byte[][] eaxEncrypt(byte[] key, byte[] nonce, byte[] header, byte[] data) throws
																							InvalidCipherTextException {
		EAXBlockCipher cipher = new EAXBlockCipher(new AESEngine());
		cipher.init(true, new AEADParameters(new KeyParameter(key), 8 * 8, nonce, header));
		
		byte[] enc = new byte[cipher.getOutputSize(data.length)];
		int len = cipher.processBytes(data, 0, data.length, enc, 0);
		cipher.doFinal(enc, len);
		
		return new byte[][] { cipher.getMac(), enc };
	}
}
