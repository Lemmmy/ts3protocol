package pw.lemmmy.ts3protocol;

import pw.lemmmy.ts3protocol.commands.CommandClientInitIV;
import pw.lemmmy.ts3protocol.packets.*;
import pw.lemmmy.ts3protocol.packets.init.*;
import pw.lemmmy.ts3protocol.utils.CryptoUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Client implements Runnable {
	private static final short DEFAULT_PORT = 9987;
	
	private InetAddress host;
	private short port;
	
	private DatagramSocket socket;
	
	private Random rand = new Random();
	private KeyPair keyPair;
	
	public Client(InetAddress host) throws SocketException {
		this(host, DEFAULT_PORT);
	}
	
	public Client(InetAddress host, short port) throws SocketException {
		this.host = host;
		this.port = port;
		
		socket = new DatagramSocket();
	}
	
	private void handshake() {
		try {
			keyPair = CryptoUtils.generateECDHKeypair();
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		
		byte[] randomBytes = new byte[4];
		rand.nextBytes(randomBytes);
		byte[] serverBytes = new byte[16]; // will be set by the server
		byte[] serverBytes2 = new byte[100];
		byte[] alpha = new byte[10]; // for clientinitiv
		rand.nextBytes(alpha);
		
		CommandClientInitIV initiv = new CommandClientInitIV(alpha, keyPair, host);
		
		System.out.println("Init0");
		sendLowLevel(new PacketInit0(randomBytes));
		System.out.println("Init1");
		receiveLowLevel(new PacketInit1(randomBytes, serverBytes));
		System.out.println("Init2");
		sendLowLevel(new PacketInit2(randomBytes, serverBytes));
		System.out.println("Init3");
		PacketInit3 init3 = new PacketInit3(serverBytes2);
		receiveLowLevel(init3);
		System.out.println("Init4");
		sendLowLevel(new PacketInit4(init3.getX(), init3.getN(), init3.getLevel(), serverBytes2, initiv));
		System.out.println("Low-level init process finished");
		
		send(new PacketCommand(initiv));
		System.out.println("High-level init packet sent");
	}
	
	private void send(Packet packet) {
		Arrays.stream(packet.write()).forEach(this::sendLowLevel);
	}
	
	private void sendLowLevel(LowLevelPacket packet) {
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
	
	private void receiveLowLevel(LowLevelPacket packet) {
		byte[] data = new byte[LowLevelPacket.PACKET_SIZE];
		DatagramPacket dp = new DatagramPacket(data, data.length);
		
		try (
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis)
		) {
			socket.receive(dp);
			packet.read(dis);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Packet getPacketFromType(PacketType type) {
		switch (type) {
			case COMMAND:
				return new PacketCommand();
		}
		
		return null;
	}
	
	private void readLoop() throws IOException {
		List<LowLevelPacket> packets = new ArrayList<>();
		boolean fragmented = false;
		
		while (true) {
			LowLevelPacket packet = new LowLevelPacket();
			receiveLowLevel(packet);
			
			// start of fragmented packet set
			if (!fragmented && packet.isFragmented()) fragmented = true;
			
			packets.add(packet);
			
			// end of fragmented packet set, or not a fragmented packet
			if (!fragmented || packet.isFragmented()) {
				PacketType type = packets.get(0).getPacketType();
				
				Packet hlPacket = getPacketFromType(type);
				if (hlPacket == null) {
					System.err.println("Don't know how to handle packet type " + type.name());
				} else {
					System.out.println("Reading packet " + type.name());
					hlPacket.read(packets.toArray(new LowLevelPacket[0]));
				}
				
				packets.clear();
			}
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
