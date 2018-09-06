package pw.lemmmy.ts3protocol.utils;

import net.i2p.crypto.eddsa.math.Curve;
import net.i2p.crypto.eddsa.math.GroupElement;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import org.bouncycastle.asn1.*;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
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
	
	public static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();
	
	public static final ECNamedCurveParameterSpec PRIME256_V1 = ECNamedCurveTable.getParameterSpec("prime256v1");
	
	public static final EdDSANamedCurveSpec CURVE25519_SPEC = EdDSANamedCurveTable.ED_25519_CURVE_SPEC;
	public static final Curve CURVE25519 = CURVE25519_SPEC.getCurve();
	
	static {
		Security.addProvider(PROVIDER);
	}
	
	public static KeyPair generateECDHKeypair() throws NoSuchAlgorithmException,
													   InvalidAlgorithmParameterException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDH", PROVIDER);
		generator.initialize(PRIME256_V1, new SecureRandom());
		return generator.generateKeyPair();
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
	
	public static String toOmega(KeyPair keyPair) throws IOException {
		return Base64.toBase64String(CryptoUtils.toDERASN1(keyPair).getEncoded());
	}
	
	public static boolean verifyECDSA(ECPublicKey key, byte[] message, byte[] proof) throws NoSuchAlgorithmException,
																							InvalidKeyException,
																							SignatureException {
		Signature signature = Signature.getInstance("SHA256withECDSA", CryptoUtils.PROVIDER);
		signature.initVerify(key);
		signature.update(message);
		return signature.verify(proof);
	}
	
	public static byte[] signECDSA(PrivateKey key, byte[] message) throws NoSuchAlgorithmException,
																							InvalidKeyException,
																							SignatureException {
		Signature signature = Signature.getInstance("SHA256withECDSA", CryptoUtils.PROVIDER);
		signature.initSign(key);
		signature.update(message);
		return signature.sign();
	}
	
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
	
	public static byte[] hashDigest(String algorithm, byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm, PROVIDER);
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
	
	public static GroupElement decompressEdPoint(byte[] key) {
		return CURVE25519.createPoint(key, true);
	}
	
	public static String hashTeamspeakPassword(String password) {
		return password == null || password.isEmpty() ? "" : Base64.toBase64String(sha1(password.getBytes()));
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
		byte[] data = sha1((omega + Long.toString(offset)).getBytes());
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
	
	public static long hashCash(KeyPair keyPair, byte level) throws IOException {
		String omega = toOmega(keyPair);
		
		long offset = 0L;
		while (offset < Long.MAX_VALUE && getHashCashLevel(omega, offset) < level) {
			offset += 1;
		}
		
		return offset;
	}
	
}
