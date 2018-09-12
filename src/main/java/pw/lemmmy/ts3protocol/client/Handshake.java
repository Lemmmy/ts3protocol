package pw.lemmmy.ts3protocol.client;

import lombok.Getter;
import net.i2p.crypto.eddsa.math.GroupElement;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.util.encoders.Base64;
import pw.lemmmy.ts3protocol.commands.CommandHandler;
import pw.lemmmy.ts3protocol.commands.handshake.*;
import pw.lemmmy.ts3protocol.crypto.ASN;
import pw.lemmmy.ts3protocol.crypto.EC;
import pw.lemmmy.ts3protocol.crypto.Hash;
import pw.lemmmy.ts3protocol.packets.command.PacketCommand;
import pw.lemmmy.ts3protocol.packets.init.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Getter
public class Handshake {
	private static final String DEFAULT_HWID = "923f136fb1e22ae6ce95e60255529c00,d13231b1bc33edfecfb9169cc7a63bcc";
	
	private static int 	IV_ALPHA_SIZE	= 10,
						SHARED_IV_SIZE	= 64,
						SHARED_MAC_SIZE	= 8;
	
	private Client client;
	private Identity identity;
	private ConnectionParameters params;
	private PacketHandler handler;
	private CommandHandler commandHandler;
	
	private SecureRandom rand = new SecureRandom();
	
	private byte[] ivAlpha = new byte[IV_ALPHA_SIZE];
	
	public Handshake(Client client) {
		this.client = client;
		this.identity = client.getIdentity();
		this.params = client.params;
		this.handler = client.packetHandler;
		this.commandHandler = client.commandHandler;
		
		addCommandListeners();
	}
	
	protected void addCommandListeners() {
		commandHandler.addCommandListener(CommandInitIVExpand2.class, this::handleInitIVExpand2);
		commandHandler.addCommandListener(CommandInitServer.class, this::handleInitServer);
	}
	
	protected void beginLowLevelHandshake() {
		byte[] randomBytes = new byte[4];
		rand.nextBytes(randomBytes);
		byte[] serverBytes = new byte[16]; // will be set by the server
		byte[] serverBytes2 = new byte[100];
		rand.nextBytes(ivAlpha);
		
		CommandClientInitIV initiv = new CommandClientInitIV(ivAlpha, identity.getKeyPair(), client.getHost());
		
		handler.sendLowLevel(new PacketInit0(randomBytes));
		handler.receiveLowLevel(new PacketInit1(randomBytes, serverBytes));
		handler.sendLowLevel(new PacketInit2(randomBytes, serverBytes));
		PacketInit3 init3 = new PacketInit3(serverBytes2);
		handler.receiveLowLevel(init3);
		handler.sendLowLevel(new PacketInit4(init3.getX(), init3.getN(), init3.getLevel(), serverBytes2, initiv));
	}
	
	private void handleInitIVExpand2(CommandInitIVExpand2 initIVExpand2) throws IOException, InvalidKeySpecException,
																				SignatureException,
																				InvalidKeyException,
																				NoSuchAlgorithmException {
		byte[] licenceBytes = Base64.decode(initIVExpand2.getArguments().get("l"));
		byte[] randomBytes = Base64.decode(initIVExpand2.getArguments().get("beta"));
		byte[] omega = Base64.decode(initIVExpand2.getArguments().get("omega"));
		byte[] proof = Base64.decode(initIVExpand2.getArguments().get("proof"));
		
		byte[] tempPrivateKey = new byte[32];
		rand.nextBytes(tempPrivateKey);
		EC.scalarClamp(tempPrivateKey);
		byte[] tempPublicKey = EC.CURVE25519_SPEC.getB().scalarMultiply(tempPrivateKey).toByteArray();
		
		byte[] ekProof = new byte[86];
		System.arraycopy(tempPublicKey, 0, ekProof, 0, 32);
		System.arraycopy(randomBytes, 0, ekProof, 32, 54);
		byte[] sign = EC.signECDSA(identity.getKeyPair().getPrivate(), ekProof);
		
		params.beginCountingPackets();
		
		CommandClientEK clientEK = new CommandClientEK(tempPublicKey, sign);
		handler.send(new PacketCommand(clientEK));
		
		ECPublicKey publicKey = ASN.fromDERASN1((ASN1Sequence) DERSequence.fromByteArray(omega));
		if (!EC.verifyECDSA(publicKey, licenceBytes, proof)) throw new RuntimeException("Licence verification failed");
		
		Licence licence;
		try (
			ByteArrayInputStream bis = new ByteArrayInputStream(licenceBytes);
			DataInputStream dis = new DataInputStream(bis)
		) {
			licence = new Licence();
			licence.parse(dis);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		GroupElement licenceKey = EC.decompressEdPoint(licence.getKey());
		byte[] sharedSecret = licenceKey.scalarMultiply(tempPrivateKey).toByteArray();
		
		byte[] sharedIV = Hash.sha512(sharedSecret);
		byte[] sharedMac = new byte[SHARED_MAC_SIZE];
		
		for (int i = 0; i < ivAlpha.length; i++)
			sharedIV[i] ^= ivAlpha[i];
		for (int i = 0; i < sharedIV.length - ivAlpha.length; i++)
			sharedIV[i + ivAlpha.length] ^= randomBytes[i];
		
		byte[] sharedIVSha1 = Hash.sha1(sharedIV);
		System.arraycopy(sharedIVSha1, 0, sharedMac, 0, 8);
		
		params.setSharedIV(sharedIV);
		params.setSharedMac(sharedMac);
		handler.ivComplete = true;
		
		CommandClientInit init = new CommandClientInit()
			.setNickname(identity.getNickname())
			.setPhoneticNickname(identity.getPhoneticNickname())
			.setVersion(Version.DEFAULT_VERSION)
			.setHardwareID(DEFAULT_HWID)
			.setKeyOffset(identity.getKeyOffset())
			.setDefaultChannelID(identity.getDefaultChannelID());
		
		handler.send(new PacketCommand(init));
	}
	
	private void handleInitServer(CommandInitServer initServer) {
		client.setID(Short.parseShort(initServer.getArguments().get("aclid")));
		client.props.readFromCommand(initServer);
		client.getServer().addUser(client);
		handler.startPinging();
		
		client.clientConnected();
	}
}
