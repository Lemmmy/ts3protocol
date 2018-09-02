package pw.lemmmy.ts3protocol;

import lombok.Getter;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.math.GroupElement;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import pw.lemmmy.ts3protocol.commands.*;
import pw.lemmmy.ts3protocol.packets.*;
import pw.lemmmy.ts3protocol.packets.init.*;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static pw.lemmmy.ts3protocol.packets.PacketDirection.CLIENT_TO_SERVER;
import static pw.lemmmy.ts3protocol.packets.PacketDirection.SERVER_TO_CLIENT;

@Getter
public class Client implements Runnable {
	private static final short DEFAULT_PORT = 9987;
	
	private InetAddress host;
	private short port;
	private DatagramSocket socket;
	
	private SecureRandom rand = new SecureRandom();
	
	private KeyPair keyPair;
	
	// temporary key/nonce until the handshake is complete
	private byte[] eaxKey = CryptoUtils.FAKE_EAX_KEY;
	private byte[] eaxNonce = CryptoUtils.FAKE_EAX_NONCE;
	
	private byte[] ivAlpha = new byte[10];
	private byte[] sharedIV = new byte[64];
	private byte[] sharedMac = new byte[8];
	
	private Map<PacketType, Integer> packetIDCounterIncoming = new HashMap<>();
	private Map<PacketType, Integer> packetIDCounterOutgoing = new HashMap<>();
	private Map<PacketType, Integer> packetGenerationCounterIncoming = new HashMap<>();
	private Map<PacketType, Integer> packetGenerationCounterOutgoing = new HashMap<>();
	private boolean countingPackets = false;
	
	private Map<Class<? extends Command>, Set<CommandListener>> commandListeners = new HashMap<>();
	
	public Client(InetAddress host) throws SocketException {
		this(host, DEFAULT_PORT);
	}
	
	public Client(InetAddress host, short port) throws SocketException {
		this.host = host;
		this.port = port;
		
		socket = new DatagramSocket();
		
		Arrays.stream(PacketType.values()).forEach(type -> {
			packetIDCounterIncoming.put(type, 0);
			packetIDCounterOutgoing.put(type, 0);
			packetGenerationCounterIncoming.put(type, 0);
			packetGenerationCounterOutgoing.put(type, 0);
		});
		
		try {
			// TODO: persist the keypair for a consistent identity
			keyPair = CryptoUtils.generateECDHKeypair();
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		
		addCommandListener(CommandInitIVExpand2.class, this::handleInitIVExpand2);
	}
	
	private void handleInitIVExpand2(CommandInitIVExpand2 initIVExpand2) throws IOException, InvalidKeySpecException,
																				SignatureException,
																				InvalidKeyException,
																				NoSuchAlgorithmException {
		byte[] licenceBytes = Base64.decode(initIVExpand2.getArguments().get("l"));
		byte[] randomBytes = Base64.decode(initIVExpand2.getArguments().get("beta"));
		byte[] omega = Base64.decode(initIVExpand2.getArguments().get("omega"));
		byte[] proof = Base64.decode(initIVExpand2.getArguments().get("proof"));
		
		System.out.println(initIVExpand2.getArguments().get("l"));
		
		ECPublicKey publicKey = CryptoUtils.fromDERASN1((ASN1Sequence) DERSequence.fromByteArray(omega));
		if (!CryptoUtils.verifyECDSA(publicKey, licenceBytes, proof)) throw new RuntimeException("Licence verification failed");
		
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
		
		GroupElement licenceKey = licence.getKey();
		// GroupElement licenceKey = CryptoUtils.decompressEdPoint(licence.getKey());
		// System.out.println("FINAL FINAL EDKEY: " + licence.getKey());
		
		byte[] seed = new byte[CryptoUtils.CURVE25519.getField().getb() / 8];
		rand.nextBytes(seed);
		
		// System.out.println(String.format("Bytes: %s", Hex.toHexString(seed)));
		
		EdDSAPrivateKeySpec privKey = new EdDSAPrivateKeySpec(seed, CryptoUtils.CURVE25519_SPEC);
		/*System.out.println(String.format("Priv key [\na=%s\nH=%s\ns=%s\n]", Hex.toHexString(privKey.geta()), Hex
			.toHexString(privKey.getH()), Hex.toHexString(privKey.getSeed())));*/
		
		byte[] sharedSecret = licenceKey.scalarMultiply(seed).toByteArray();
		System.out.println(String.format("Shared secret (%d): %s", sharedSecret.length, Hex.toHexString(sharedSecret)));
		
		sharedIV = CryptoUtils.sha512(sharedSecret);
		
		for (int i = 0; i < 10; i++) sharedIV[i]      ^= ivAlpha[i];
		for (int i = 0; i < 54; i++) sharedIV[i + 10] ^= randomBytes[i];
		
		byte[] sharedIVSha1 = CryptoUtils.sha1(sharedIV);
		System.arraycopy(sharedIVSha1, 0, sharedMac, 0, 8);
		
		// System.out.println(String.format("Shared IV (%d): %s", sharedIV.length, Hex.toHexString(sharedIV)));
		// System.out.println(String.format("Shared MAC (%d): %s", sharedMac.length, Hex.toHexString(sharedMac)));
		
		byte[] ekProof = new byte[seed.length + randomBytes.length];
		System.arraycopy(seed, 0, ekProof, 0, seed.length);
		System.arraycopy(randomBytes, 0, ekProof, 32, randomBytes.length);
		byte[] ekProofSigned = CryptoUtils.signEDDSA(new EdDSAPrivateKey(privKey), ekProof);
		// System.out.println(String.format("EK raw (%d): %s", ekProof.length, Hex.toHexString(ekProof)));
		// System.out.println(String.format("EK signed (%d): %s", ekProofSigned.length, Hex.toHexString
		// (ekProofSigned)));
		
		packetIDCounterIncoming.put(PacketType.COMMAND, 1);
		countingPackets = true;
		
		CommandClientEK clientEK = new CommandClientEK(seed, ekProofSigned);
		send(new PacketCommand(clientEK));
	}
	
	private void handshake() {
		byte[] randomBytes = new byte[4];
		rand.nextBytes(randomBytes);
		byte[] serverBytes = new byte[16]; // will be set by the server
		byte[] serverBytes2 = new byte[100];
		rand.nextBytes(ivAlpha);
		
		CommandClientInitIV initiv = new CommandClientInitIV(ivAlpha, keyPair, host);
		
		sendLowLevel(new PacketInit0(randomBytes));
		receiveLowLevel(new PacketInit1(randomBytes, serverBytes));
		sendLowLevel(new PacketInit2(randomBytes, serverBytes));
		PacketInit3 init3 = new PacketInit3(serverBytes2);
		receiveLowLevel(init3);
		sendLowLevel(new PacketInit4(init3.getX(), init3.getN(), init3.getLevel(), serverBytes2, initiv));
	}
	
	public <T extends Command> void addCommandListener(Class<T> commandClass, CommandListener<T> listener) {
		if (!commandListeners.containsKey(commandClass)) {
			commandListeners.put(commandClass, new HashSet<>());
		}
		
		commandListeners.get(commandClass).add(listener);
	}
	
	public void handleCommand(Command command) {
		if (!commandListeners.containsKey(command.getClass())) return;
		commandListeners.get(command.getClass()).forEach(l -> {
			try {
				l.handle(command);
			} catch (Exception e) {
				System.err.println("Error in command handler for " + command.getName());
				e.printStackTrace();
			}
		});
	}
	
	private void readLoop() throws IOException {
		List<LowLevelPacket> packets = new ArrayList<>();
		boolean fragmented = false;
		
		while (true) {
			LowLevelPacket packet = new LowLevelPacket();
			packet.setDirection(SERVER_TO_CLIENT);
			receiveLowLevel(packet);
			
			// start of fragmented packet set
			if (!fragmented && packet.isFragmented()) fragmented = true;
			
			packets.add(packet);
			
			// end of fragmented packet set, or not a fragmented packet
			if (!fragmented || packet.isFragmented()) {
				PacketType type = packets.get(0).getPacketType();
				
				Optional<Packet> hlPacketOpt = getPacketFromType(type);
				if (hlPacketOpt.isPresent()) {
					Packet hlPacket = hlPacketOpt.get();
					System.out.println("Reading packet " + type.name());
					hlPacket.read(this, packets.toArray(new LowLevelPacket[0]));
				} else {
					System.err.println("Don't know how to handle packet type " + type.name());
				}
				
				packets.clear();
			}
		}
	}
	
	public int incrementPacketCounter(PacketType type, Map<PacketType, Integer> counter, Map<PacketType, Integer>
		generation) {
		if (countingPackets) {
			int current = counter.get(type);
			
			if (current >= 65535) {
				counter.put(type, 0);
				generation.put(type, generation.get(type) + 1);
			} else {
				counter.put(type, current + 1);
			}
			
			return counter.get(type);
		} else {
			return -1;
		}
	}
	
	private void receiveLowLevel(LowLevelPacket packet) {
		packet.setDirection(SERVER_TO_CLIENT);
		
		byte[] data = new byte[LowLevelPacket.PACKET_SIZE];
		DatagramPacket dp = new DatagramPacket(data, data.length);
		
		try (
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis)
		) {
			socket.receive(dp);
			packet.read(dis, dp.getLength());
			
			incrementPacketCounter(packet.getPacketType(), packetIDCounterIncoming, packetGenerationCounterIncoming);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send(Packet packet) {
		packet.setDirection(CLIENT_TO_SERVER);
		Arrays.stream(packet.write(this)).forEach(this::sendLowLevel);
	}
	
	public void sendLowLevel(LowLevelPacket packet) {
		packet.setDirection(CLIENT_TO_SERVER);
		
		try (
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos)
		) {
			packet.write(dos);
			byte[] data = bos.toByteArray();
			socket.send(new DatagramPacket(data, data.length, host, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Optional<Packet> getPacketFromType(PacketType type) {
		switch (type) {
			case ACK:
				return Optional.of(new PacketAck());
			case COMMAND:
				return Optional.of(new PacketCommand());
			default:
				return Optional.empty();
		}
	}
	
	@Override
	public void run() {
		try {
			handshake();
			readLoop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
