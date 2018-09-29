package pw.lemmmy.ts3protocol.crypto;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.*;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Slf4j
public class ASN {
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
			factory = KeyFactory.getInstance("ECDH", Crypto.PROVIDER);
		} catch (NoSuchAlgorithmException e) {
			log.error("Provider doesn't provide ECDH algorithm", e);
			return null;
		}
		
		ASN1Integer x = (ASN1Integer) seq.getObjectAt(2);
		ASN1Integer y = (ASN1Integer) seq.getObjectAt(3);
		
		ECPoint point = Crypto.PRIME256_V1.getCurve().createPoint(x.getValue(), y.getValue());
		ECPublicKeySpec key = new ECPublicKeySpec(point, Crypto.PRIME256_V1);
		return (ECPublicKey) factory.generatePublic(key);
	}
	
	public static String encodeBase64ASN(KeyPair keyPair) throws IOException {
		return Base64.toBase64String(toDERASN1(keyPair).getEncoded());
	}
}
