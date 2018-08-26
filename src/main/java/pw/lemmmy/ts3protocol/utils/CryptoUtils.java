package pw.lemmmy.ts3protocol.utils;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.*;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static pw.lemmmy.ts3protocol.packets.LowLevelPacket.MAC_SIZE;

public class CryptoUtils {
	public static final byte[] FAKE_EAX_KEY = { 0x63, 0x3A, 0x5C, 0x77, 0x69, 0x6E, 0x64, 0x6F, 0x77,
		0x73, 0x5C, 0x73, 0x79, 0x73, 0x74, 0x65 };
	
	public static final byte[] FAKE_EAX_NONCE = { 0x6D, 0x5C, 0x66, 0x69, 0x72, 0x65, 0x77, 0x61, 0x6C,
		0x6C, 0x33, 0x32, 0x2E, 0x63, 0x70, 0x6C };
	
	public static final byte[] HANDSHAKE_MAC = { 0x54, 0x53, 0x33, 0x49, 0x4E, 0x49, 0x54, 0x31 };
	
	public static final ECNamedCurveParameterSpec PRIME256_V1 = ECNamedCurveTable.getParameterSpec("prime256v1");
	public static final ECDomainParameters PRIME256_V1_DOMAIN = new ECDomainParameters(
		PRIME256_V1.getCurve(), PRIME256_V1.getG(), PRIME256_V1.getN(), PRIME256_V1.getH()
	);
	public static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();
	
	public static KeyPair generateECDHKeypair() throws NoSuchAlgorithmException,
													   InvalidAlgorithmParameterException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDH", PROVIDER);
		generator.initialize(PRIME256_V1, new SecureRandom());
		KeyPair keyPair = generator.generateKeyPair();
		
		System.out.println("Pub: " + Hex.encodeHexString(keyPair.getPublic().getEncoded()));
		System.out.println("Priv: " + Hex.encodeHexString(keyPair.getPrivate().getEncoded()));
		
		return keyPair;
	}
	
	public static DERSequence toDERASN1(KeyPair kp) {
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
	
	public static ECPublicKey fromDERASN1(ASN1Sequence seq) throws InvalidKeySpecException {
		KeyFactory factory;
		try {
			factory = KeyFactory.getInstance("ECDH", PROVIDER);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		ASN1Integer x = (ASN1Integer) seq.getObjectAt(2);
		ASN1Integer y = (ASN1Integer) seq.getObjectAt(3);
		
		ECPoint point = PRIME256_V1.getCurve().createPoint(x.getValue(), y.getValue());
		ECPublicKeySpec key = new ECPublicKeySpec(point, PRIME256_V1);
		return (ECPublicKey) factory.generatePublic(key);
	}
	
	public static boolean verifyECDSA(ECPublicKey key, byte[] message, byte[] proof) throws NoSuchAlgorithmException,
																							InvalidKeyException,
																							SignatureException {
		Signature signature = Signature.getInstance("SHA256withECDSA", CryptoUtils.PROVIDER);
		signature.initVerify(key);
		signature.update(message);
		return signature.verify(proof);
	}
	
	public static byte[][] eaxEncrypt(byte[] key, byte[] nonce, byte[] header, byte[] data) throws
																							InvalidCipherTextException {
		EAXBlockCipher cipher = new EAXBlockCipher(new AESEngine());
		cipher.init(true, new AEADParameters(new KeyParameter(key), MAC_SIZE * 8, nonce, header));
		
		byte[] enc = new byte[cipher.getOutputSize(data.length)];
		int len = cipher.processBytes(data, 0, data.length, enc, 0);
		cipher.doFinal(enc, len);
		
		return new byte[][] { cipher.getMac(), enc };
	}
	
	public static byte[] eaxDecrypt(byte[] key, byte[] nonce, byte[] header, byte[] data, byte[] mac) {
		EAXBlockCipher cipher = new EAXBlockCipher(new AESEngine());
		cipher.init(false, new AEADParameters(new KeyParameter(key), mac.length * 8, nonce, header));
		
		byte[] dec = new byte[cipher.getOutputSize(data.length + mac.length)];
		
		try {
			int len = cipher.processBytes(data, 0, data.length, dec, 0);
			cipher.processBytes(mac, 0, mac.length, dec, 0);
			cipher.doFinal(dec, len);
		} catch (InvalidCipherTextException e) {
			System.err.println("Failed to decrypt data - MAC check failed or data was invalid:");
			e.printStackTrace();
		}
		
		return dec;
	}
}
