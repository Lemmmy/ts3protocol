package pw.lemmmy.ts3protocol.client;

import lombok.extern.slf4j.Slf4j;
import pw.lemmmy.ts3protocol.packets.LowLevelPacket;
import pw.lemmmy.ts3protocol.packets.Packet;
import pw.lemmmy.ts3protocol.packets.PacketType;
import pw.lemmmy.ts3protocol.packets.ack.PacketAck;
import pw.lemmmy.ts3protocol.packets.ack.PacketAckLow;
import pw.lemmmy.ts3protocol.packets.ack.PacketPing;
import pw.lemmmy.ts3protocol.packets.ack.PacketPong;
import pw.lemmmy.ts3protocol.packets.command.PacketCommand;
import pw.lemmmy.ts3protocol.packets.command.PacketCommandLow;
import pw.lemmmy.ts3protocol.packets.voice.PacketVoice;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static pw.lemmmy.ts3protocol.packets.PacketDirection.CLIENT_TO_SERVER;
import static pw.lemmmy.ts3protocol.packets.PacketDirection.SERVER_TO_CLIENT;

@Slf4j
public class PacketHandler {
	private static final long PING_INTERVAL = 3;
	private static final int SEND_ERROR_THRESHOLD = 5;
	
	private Client client;
	private ConnectionParameters params;
	private DatagramSocket socket;
	
	protected boolean ivComplete;
	
	private ScheduledFuture<?> pingFuture;
	
	private int sendErrors = 0;
	
	public PacketHandler(Client client) {
		this.client = client;
		this.params = client.params;
		this.socket = client.getSocket();
	}
	
	protected void startPinging() {
		pingFuture = Client.EXECUTOR.scheduleAtFixedRate(() -> {
			log.trace("Sending ping");
			send(new PacketPing());
		}, PING_INTERVAL, PING_INTERVAL, TimeUnit.SECONDS);
	}
	
	protected void stopPinging() { // TODO: deal with closing
		pingFuture.cancel(false);
	}
	
	void readLoop() {
		List<LowLevelPacket> packets = new ArrayList<>(), fragmentedPackets = new ArrayList<>();
		boolean fragmented = false;
		
		while (true) {
			try {
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
						log.trace("Received ping, sending pong " + packet.getPacketID());
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
			} catch (Exception e) {
				log.error("Error reading packet", e);
			}
		}
	}
	
	private void readPackets(List<LowLevelPacket> packets) throws IOException {
		PacketType type = packets.get(0).getPacketType();
		
		Optional<Packet> hlPacketOpt = getPacketFromType(type);
		if (hlPacketOpt.isPresent()) {
			Packet hlPacket = hlPacketOpt.get();
			hlPacket.setDirection(SERVER_TO_CLIENT);
			hlPacket.read(client, packets.toArray(new LowLevelPacket[0]));
		} else {
			log.error("Don't know how to handle packet type {}", type.name());
		}
	}
	
	void receiveLowLevel(LowLevelPacket packet) {
		packet.setDirection(SERVER_TO_CLIENT);
		
		if (socket.isClosed()) {
			log.error("Socket has been closed. Exiting.");
			client.disconnect(1);
			return;
		}
		
		byte[] data = new byte[LowLevelPacket.PACKET_SIZE];
		DatagramPacket dp = new DatagramPacket(data, data.length);
		
		try (
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis)
		) {
			socket.receive(dp);
			packet.read(dis, dp.getLength());
			
			params.setPacketCounter(packet.getPacketID(), packet.getPacketType());
		} catch (IOException e) {
			log.error("Error reading low-level packet", e);
		}
	}
	
	public void send(Packet packet) {
		if (packet.getPacketType().isEncrypted() && ivComplete) {
			packet.setUnencrypted(false);
		}
		
		packet.setDirection(CLIENT_TO_SERVER);
		Arrays.stream(packet.write(client)).forEach(this::sendLowLevel);
	}
	
	public void sendLowLevel(LowLevelPacket packet) {
		packet.setDirection(CLIENT_TO_SERVER);
		
		if (socket.isClosed()) {
			log.error("Socket has been closed. Exiting.");
			client.disconnect(1);
			return;
		}
		
		try (
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos)
		) {
			packet.write(dos);
			byte[] data = bos.toByteArray();
			socket.send(new DatagramPacket(data, data.length, client.getHost(), client.getPort()));
			
			sendErrors = 0;
		} catch (IOException e) {
			log.error("Error sending low-level packet", e);
			
			sendErrors++;
			
			if (sendErrors >= SEND_ERROR_THRESHOLD) {
				log.error("Reached threshold for packet send errors. Assuming connection lost. Exiting.");
				client.disconnect(1);
			}
		}
	}
	
	private Optional<Packet> getPacketFromType(PacketType type) {
		switch (type) {
			case VOICE:
				return Optional.of(new PacketVoice());
			case COMMAND:
				return Optional.of(new PacketCommand());
			case COMMAND_LOW:
				return Optional.of(new PacketCommandLow());
			case PING:
				return Optional.of(new PacketPing());
			case PONG:
				return Optional.of(new PacketPong());
			case ACK:
				return Optional.of(new PacketAck());
			case ACK_LOW:
				return Optional.of(new PacketAckLow());
			default:
				return Optional.empty();
		}
	}
	
	public boolean shouldEncryptPackets() {
		return ivComplete;
	}
}
