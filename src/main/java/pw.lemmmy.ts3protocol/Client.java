package pw.lemmmy.ts3protocol;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.CommandClientInitIV;
import pw.lemmmy.ts3protocol.commands.CommandListener;
import pw.lemmmy.ts3protocol.packets.*;
import pw.lemmmy.ts3protocol.packets.init.*;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
	
	private Map<Class<? extends Command>, Set<CommandListener>> commandListeners;
	
	public Client(InetAddress host) throws SocketException {
		this(host, DEFAULT_PORT);
	}
	
	public Client(InetAddress host, short port) throws SocketException {
		this.host = host;
		this.port = port;
		
		socket = new DatagramSocket();
		
		try {
			// TODO: persist the keypair for a consistent identity
			keyPair = CryptoUtils.generateECDHKeypair();
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	private void handshake() {
		byte[] randomBytes = new byte[4];
		rand.nextBytes(randomBytes);
		byte[] serverBytes = new byte[16]; // will be set by the server
		byte[] serverBytes2 = new byte[100];
		byte[] alpha = new byte[10]; // for clientinitiv
		rand.nextBytes(alpha);
		
		CommandClientInitIV initiv = new CommandClientInitIV(alpha, keyPair, host);
		
		sendLowLevel(new PacketInit0(randomBytes));
		receiveLowLevel(new PacketInit1(randomBytes, serverBytes));
		sendLowLevel(new PacketInit2(randomBytes, serverBytes));
		PacketInit3 init3 = new PacketInit3(serverBytes2);
		receiveLowLevel(init3);
		sendLowLevel(new PacketInit4(init3.getX(), init3.getN(), init3.getLevel(), serverBytes2, initiv));
		System.out.println("Low-level init process finished");
		
		send(new PacketCommand(initiv));
		System.out.println("High-level init packet sent");
	}
	
	public void addCommandListener(Class<? extends Command> commandClass, CommandListener listener) {
		if (!commandListeners.containsKey(commandClass)) {
			commandListeners.put(commandClass, new HashSet<>());
		}
		
		commandListeners.get(commandClass).add(listener);
	}
	
	public void handleCommand(Command command) {
		if (!commandListeners.containsKey(command.getClass())) return;
		commandListeners.get(command.getClass()).forEach(l -> l.handle(command));
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void send(Packet packet) {
		packet.setDirection(CLIENT_TO_SERVER);
		Arrays.stream(packet.write(this)).forEach(this::sendLowLevel);
	}
	
	private void sendLowLevel(LowLevelPacket packet) {
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
