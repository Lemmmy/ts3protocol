package pw.lemmmy.ts3protocol.client;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.CommandHandler;
import pw.lemmmy.ts3protocol.server.Server;
import pw.lemmmy.ts3protocol.voice.VoiceHandler;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class Client implements Runnable {
	public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static final short DEFAULT_PORT = 9987;
	
	private InetAddress host;
	private short port;
	private DatagramSocket socket;
	
	private Server server;
	private Identity identity;
	
	private ConnectionParameters params;
	private PacketHandler packetHandler;
	private CommandHandler commandHandler;
	private Handshake handshake;
	private VoiceHandler voiceHandler;
	
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
		voiceHandler = new VoiceHandler(this);
		
		voiceHandler.init();
		
		server = new Server(this);
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
