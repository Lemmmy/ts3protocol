package pw.lemmmy.ts3protocol.crypto;

import net.i2p.crypto.eddsa.math.Curve;
import net.i2p.crypto.eddsa.math.GroupElement;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class EC {
	public static final EdDSANamedCurveSpec CURVE25519_SPEC = EdDSANamedCurveTable.ED_25519_CURVE_SPEC;
	public static final Curve CURVE25519 = CURVE25519_SPEC.getCurve();
	
	public static KeyPair generateECDHKeypair() throws NoSuchAlgorithmException,
													   InvalidAlgorithmParameterException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDH", Crypto.PROVIDER);
		generator.initialize(Crypto.PRIME256_V1, new SecureRandom());
		return generator.generateKeyPair();
	}
	
	public static KeyPair decodeECDHKeypair(byte[] publicBytes, byte[] privateBytes) throws NoSuchAlgorithmException,
																							InvalidKeySpecException {
		KeyFactory factory = KeyFactory.getInstance("ECDH", Crypto.PROVIDER);
		PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(publicBytes));
		PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
		return new KeyPair(publicKey, privateKey);
	}
	
	public static boolean verifyECDSA(ECPublicKey key, byte[] message, byte[] proof) throws NoSuchAlgorithmException,
																							InvalidKeyException,
																							SignatureException {
		Signature signature = Signature.getInstance("SHA256withECDSA", Crypto.PROVIDER);
		signature.initVerify(key);
		signature.update(message);
		return signature.verify(proof);
	}
	
	public static byte[] signECDSA(PrivateKey key, byte[] message) throws NoSuchAlgorithmException,
																		  InvalidKeyException,
																		  SignatureException {
		Signature signature = Signature.getInstance("SHA256withECDSA", Crypto.PROVIDER);
		signature.initSign(key);
		signature.update(message);
		return signature.sign();
	}
	
	public static GroupElement decompressEdPoint(byte[] key) {
		return CURVE25519.createPoint(key, true);
	}
	
	public static void scalarClamp(byte[] bytes) {
		bytes[0]  &= 0xF8;
		bytes[31] &= 0x3F;
		bytes[31] |= 0x40;
	}
}
