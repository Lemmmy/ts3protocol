package pw.lemmmy.ts3protocol.client;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.CommandHandler;
import pw.lemmmy.ts3protocol.commands.channels.CommandChannelListFinished;
import pw.lemmmy.ts3protocol.commands.channels.CommandChannelSubscribeAll;
import pw.lemmmy.ts3protocol.server.Server;
import pw.lemmmy.ts3protocol.users.User;
import pw.lemmmy.ts3protocol.voice.VoiceHandler;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class Client extends User {
	public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static final short DEFAULT_PORT = 9987;
	
	private InetAddress host;
	private short port;
	private DatagramSocket socket;
	
	private Identity identity;
	
	public final ConnectionParameters params;
	public final PacketHandler packetHandler;
	public final CommandHandler commandHandler;
	public final Handshake handshake;
	public final VoiceHandler voiceHandler;
	
	private boolean clientConnected, clientReady;
	private Set<ClientConnectedHandler> clientConnectedHandlers = new HashSet<>();
	private Set<ClientReadyHandler> clientReadyHandlers = new HashSet<>();
	
	public Client(Identity identity, InetAddress host) throws SocketException {
		this(identity, host, DEFAULT_PORT);
	}
	
	public Client(Identity identity, InetAddress host, short port) throws SocketException {
		super();
		
		this.identity = identity;
		this.host = host;
		this.port = port;
		
		socket = new DatagramSocket();
		
		params = new ConnectionParameters(this);
		packetHandler = new PacketHandler(this);
		commandHandler = new CommandHandler(this);
		handshake = new Handshake(this);
		voiceHandler = new VoiceHandler(this);
		
		voiceHandler.init();
		
		server = new Server(this);
		setClient(this);
		
		props.set(Nickname.class, identity.getNickname());
		props.set(PhoneticNickname.class, identity.getPhoneticNickname());
		
		// TODO: check multiple things (servergroups, users, channels)
		commandHandler.addCommandListener(CommandChannelListFinished.class, c -> clientReady());
	}
	
	public void run() {
		EXECUTOR.submit(() -> {
			try {
				handshake.beginLowLevelHandshake();
				packetHandler.readLoop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void onClientConnected(ClientConnectedHandler handler) {
		if (clientConnected) {
			handler.handle(this);
		} else {
			clientConnectedHandlers.add(handler);
		}
	}
	
	public void clientConnected() {
		if (clientConnected) return;
		clientConnected = true;
		commandHandler.send(new CommandChannelSubscribeAll());
		clientConnectedHandlers.forEach(h -> h.handle(this));
		clientConnectedHandlers.clear();
	}
	
	public void onClientReady(ClientReadyHandler handler) {
		if (clientConnected) {
			handler.handle(this);
		} else {
			clientReadyHandlers.add(handler);
		}
	}
	
	public void clientReady() {
		if (clientReady) return;
		clientReady = true;
		clientReadyHandlers.forEach(h -> h.handle(this));
		clientReadyHandlers.clear();
	}
	
	@FunctionalInterface
	public interface ClientConnectedHandler {
		void handle(Client client);
	}
	
	@FunctionalInterface
	public interface ClientReadyHandler {
		void handle(Client client);
	}
}
