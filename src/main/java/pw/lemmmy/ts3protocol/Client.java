package pw.lemmmy.ts3protocol;

import lombok.Getter;
import net.i2p.crypto.eddsa.math.GroupElement;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.util.encoders.Base64;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.CommandListener;
import pw.lemmmy.ts3protocol.commands.handshake.*;
import pw.lemmmy.ts3protocol.packets.LowLevelPacket;
import pw.lemmmy.ts3protocol.packets.Packet;
import pw.lemmmy.ts3protocol.packets.PacketType;
import pw.lemmmy.ts3protocol.packets.ack.PacketAck;
import pw.lemmmy.ts3protocol.packets.ack.PacketAckLow;
import pw.lemmmy.ts3protocol.packets.ack.PacketPing;
import pw.lemmmy.ts3protocol.packets.ack.PacketPong;
import pw.lemmmy.ts3protocol.packets.command.PacketCommand;
import pw.lemmmy.ts3protocol.packets.command.PacketCommandLow;
import pw.lemmmy.ts3protocol.packets.init.*;
import pw.lemmmy.ts3protocol.utils.CachedKey;
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
	private static final String DEFAULT_HWID = "923f136fb1e22ae6ce95e60255529c00,d13231b1bc33edfecfb9169cc7a63bcc";
	
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
	private boolean ivComplete = false;
	
	private short clientID = 0;
	
	private Map<PacketType, Integer> 	packetIDCounterIncoming = new HashMap<>(),
										packetIDCounterOutgoing = new HashMap<>(),
										packetGenerationCounterIncoming = new HashMap<>(),
										packetGenerationCounterOutgoing = new HashMap<>();
	private Map<PacketType, CachedKey> 	keyCacheIncoming = new HashMap<>(),
										keyCacheOutgoing = new HashMap<>();
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
			packetIDCounterIncoming.put(type, 1);
			packetIDCounterOutgoing.put(type, 1);
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
		addCommandListener(CommandInitServer.class, this::handleInitServer);
	}
	
	private void handleInitServer(CommandInitServer initServer) {
		clientID = Short.parseShort(initServer.getArguments().get("aclid"));
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
		tempPrivateKey[0]  &= 0xF8;
		tempPrivateKey[31] &= 0x3F;
		tempPrivateKey[31] |= 0x40;
		byte[] tempPublicKey = CryptoUtils.CURVE25519_SPEC.getB().scalarMultiply(tempPrivateKey).toByteArray();
		
		byte[] ekProof = new byte[86];
		System.arraycopy(tempPublicKey, 0, ekProof, 0, 32);
		System.arraycopy(randomBytes, 0, ekProof, 32, 54);
		byte[] sign = CryptoUtils.signECDSA(keyPair.getPrivate(), ekProof);
		
		packetIDCounterIncoming.put(PacketType.COMMAND, 1);
		packetIDCounterOutgoing.put(PacketType.COMMAND, 0);
		countingPackets = true;
		
		CommandClientEK clientEK = new CommandClientEK(tempPublicKey, sign);
		send(new PacketCommand(clientEK));
		
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
		
		GroupElement licenceKey = CryptoUtils.decompressEdPoint(licence.getKey());
		byte[] sharedSecret = licenceKey.scalarMultiply(tempPrivateKey).toByteArray();
		
		sharedIV = CryptoUtils.sha512(sharedSecret);
		
		for (int i = 0; i < 10; i++) sharedIV[i]      ^= ivAlpha[i];
		for (int i = 0; i < 54; i++) sharedIV[i + 10] ^= randomBytes[i];
		
		byte[] sharedIVSha1 = CryptoUtils.sha1(sharedIV);
		System.arraycopy(sharedIVSha1, 0, sharedMac, 0, 8);
		
		ivComplete = true;
		
		CommandClientInit init = new CommandClientInit()
			.setNickname("Poopy bot")
			.setVersion(Version.DEFAULT_VERSION)
			.setHardwareID(DEFAULT_HWID)
			.setKeyOffset(CryptoUtils.hashCash(keyPair, (byte) 8));
		
		send(new PacketCommand(init));
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
		List<LowLevelPacket> packets = new ArrayList<>(), fragmentedPackets = new ArrayList<>();
		boolean fragmented = false;
		
		while (true) {
			LowLevelPacket packet = new LowLevelPacket();
			packet.setDirection(SERVER_TO_CLIENT);
			receiveLowLevel(packet);
			
			// send corresponding acknowledgement packets. this has to be done during fragmentation, not HL parsing
			switch (packet.getPacketType()) {
				case COMMAND:
					send(new PacketAck(packet.getPacketID()));
					break;
				case COMMAND_LOW:
					send(new PacketAckLow(packet.getPacketID()));
					break;
				case PING:
					send(new PacketPong(packet.getPacketID()));
					break;
			}
			
			if (packet.getPacketType().isFragmentable()) {
				// start of fragmented packet set
				if (packet.isFragmented()) {
					fragmented = !fragmented;
				}
				
				fragmentedPackets.add(packet);
				
				// end of fragmented packet set, or not a fragmented packet
				if (!fragmented) {
					readPackets(fragmentedPackets);
					fragmentedPackets.clear();
				}
			} else {
				packets.add(packet);
				readPackets(packets);
				packets.clear();
			}
		}
	}
	
	private void readPackets(List<LowLevelPacket> packets) throws IOException {
		PacketType type = packets.get(0).getPacketType();
		
		Optional<Packet> hlPacketOpt = getPacketFromType(type);
		if (hlPacketOpt.isPresent()) {
			Packet hlPacket = hlPacketOpt.get();
			hlPacket.setDirection(SERVER_TO_CLIENT);
			hlPacket.read(this, packets.toArray(new LowLevelPacket[0]));
		} else {
			System.err.println("Don't know how to handle packet type " + type.name());
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
		if (ivComplete && packet.getPacketType().isEncrypted()) packet.setUnencrypted(false);
		
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
			case ACK_LOW:
				return Optional.of(new PacketAckLow());
			case PING:
				return Optional.of(new PacketPing());
			case PONG:
				return Optional.of(new PacketPong());
			case COMMAND:
				return Optional.of(new PacketCommand());
			case COMMAND_LOW:
				return Optional.of(new PacketCommandLow());
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
