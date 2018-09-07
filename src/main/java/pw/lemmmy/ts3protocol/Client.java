package pw.lemmmy.ts3protocol;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.CommandHandler;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

@Getter
public class Client implements Runnable {
	private static final short DEFAULT_PORT = 9987;
	
	private InetAddress host;
	private short port;
	private DatagramSocket socket;
	
	private Identity identity;
	
	private ConnectionParameters params;
	private PacketHandler packetHandler;
	private CommandHandler commandHandler;
	private Handshake handshake;
	
	public Client(Identity identity, InetAddress host) throws SocketException {
		this(identity, host, DEFAULT_PORT);
	}
	
	public Client(Identity identity, InetAddress host, short port) throws SocketException {
		this.identity = identity;
		this.host = host;
		this.port = port;
		
		socket = new DatagramSocket();
		params = new ConnectionParameters(this);
		packetHandler = new PacketHandler(this);
		commandHandler = new CommandHandler();
		handshake = new Handshake(this);
	}
	
	@Override
	public void run() {
		try {
			handshake.beginLowLevelHandshake();
			packetHandler.readLoop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
