package pw.lemmmy.ts3protocol.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pw.lemmmy.ts3protocol.channels.Channel;
import pw.lemmmy.ts3protocol.commands.CommandClientDisconnect;
import pw.lemmmy.ts3protocol.commands.CommandHandler;
import pw.lemmmy.ts3protocol.commands.channels.CommandChannelListFinished;
import pw.lemmmy.ts3protocol.commands.channels.CommandChannelSubscribeAll;
import pw.lemmmy.ts3protocol.commands.clients.CommandNotifyClientLeftView;
import pw.lemmmy.ts3protocol.commands.errors.CommandError;
import pw.lemmmy.ts3protocol.declarations.TS3Error;
import pw.lemmmy.ts3protocol.declarations.TS3Reason;
import pw.lemmmy.ts3protocol.server.CodecEncryptionMode;
import pw.lemmmy.ts3protocol.server.Server;
import pw.lemmmy.ts3protocol.users.User;
import pw.lemmmy.ts3protocol.utils.properties.PropertyManager;
import pw.lemmmy.ts3protocol.voice.VoiceHandler;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
@Slf4j
public class Client extends User {
	public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(3);
	
	private static final short DEFAULT_PORT = 9987;
	private static final int CONNECT_TIMEOUT_SECONDS = 5; // TODO: configurable
	
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
	private Map<TS3Error, Set<ErrorHandler>> errorHandlers = new HashMap<>();
	
	private Future<?> readLoopFuture;
	private volatile boolean disconnecting = false;
	
	@Setter private String disconnectMessage;
	
	@Getter(AccessLevel.NONE)
	private PropertyManager.ChangeListener<Boolean> channelCodecListener;
	
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
		
		initErrors();
		initEventHandlers();
		
		Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));
	}
	
	private void initErrors() {
		// Pass error commands through to error handlers
		commandHandler.addCommandListener(CommandError.class, c -> c.getArgumentSets().forEach(args -> {
			if (!args.containsKey("id")) {
				log.error(ansi().render("@|bold,red Received unexpected and unknown error packet.|@").toString());
				return;
			}
			
			int errorID = Integer.parseInt(args.get("id"));
			if (TS3Error.ERROR_MAP.containsKey(errorID)) {
				TS3Error error = TS3Error.ERROR_MAP.get(errorID);
				
				if (errorHandlers.containsKey(error)) {
					errorHandlers.get(error).forEach(h -> h.handle(this, error, c));
				} else {
					log.error(ansi().render("@|red Received unhandled error |@@|bold,red {} |@@|red with message: |@@|bold,red {}|@").toString(), error.name(), args.get("msg"));
				}
			} else {
				log.error(ansi().render("@|red Received unknown error type |@@|bold,red {} |@@|red with message: |@@|bold,red {}|@").toString(), errorID, args.get("msg"));
			}
		}));
		
		// Handle 'kicked from server' and other disconnections
		commandHandler.addCommandListener(CommandNotifyClientLeftView.class, c -> c.getArgumentSets().forEach(args -> {
			if (!args.containsKey("clid") || !args.containsKey("ctid")) return;
			
			short clientID = Short.parseShort(args.get("clid"));
			if (clientID != getID()) return;
			
			TS3Reason reason = TS3Reason.getReasonFromCommand(c)
				.orElseThrow(() -> new RuntimeException("Unable to get kick reason from command"));
			String msg = args.getOrDefault("reasonmsg", "Unknown reason");
			String invokerName = args.getOrDefault("invokername", "Unknown");
			
			switch (reason) {
				case KICK_SERVER:
					fatal("Kicked from server by {}: {}", invokerName, msg);
					break;
				case KICK_SERVER_BAN:
					fatal("Banned from server by {}: {}", invokerName, msg);
					break;
				case SERVER_STOPPED:
					fatal("Server stopped.");
					break;
				case LOST_CONNECTION:
					fatal("Lost connection.");
					break;
				default:
					fatal("Disconnected for unhandled reason {}.", reason.name());
					break;
			}
		}));
		
		// Handle some basic client errors
		onError(TS3Error.CLIENT_HACKED, new FatalErrorHandler("The server thinks the client is modified - invalid version string?"));
		onError(TS3Error.CLIENT_TOO_MANY_CLONES_CONNECTED, new FatalErrorHandler("A client is already connected to the server."));
		onError(TS3Error.CLIENT_VERSION_OUTDATED, new FatalErrorHandler("The client version is outdated, please update."));
		onError(TS3Error.SERVER_VERSION_OUTDATED, new FatalErrorHandler("The server version is outdated, please update."));
		onError(TS3Error.SERVER_IS_SHUTTING_DOWN, new FatalErrorHandler("The server is shutting down."));
		onError(TS3Error.SERVER_MAXCLIENTS_REACHED, new FatalErrorHandler("The server is full."));
	}
	
	private void initEventHandlers() {
		// TODO: check multiple things (servergroups, users, channels)
		commandHandler.addCommandListener(CommandChannelListFinished.class, c -> clientReady());
		
		onChannelChanged(this::channelChanged);
		server.getProps().addChangeListener(Server.VoiceEncryptionMode.class, c -> checkVoiceEncryption());
	}
	
	public void run() {
		EXECUTOR.schedule(() -> {
			if (!clientReady) {
				log.error("Client was not ready within {} seconds. Please retry.", CONNECT_TIMEOUT_SECONDS);
				System.exit(1);
			}
		}, CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		
		readLoopFuture = EXECUTOR.submit(() -> {
			handshake.beginLowLevelHandshake();
			packetHandler.readLoop();
		});
	}
	
	public void onClientConnected(ClientConnectedHandler handler) {
		if (clientConnected) {
			handler.handle(this);
		} else {
			clientConnectedHandlers.add(handler);
		}
	}
	
	void clientConnected() {
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
	
	private void clientReady() {
		if (clientReady) return;
		clientReady = true;
		clientReadyHandlers.forEach(h -> h.handle(this));
		clientReadyHandlers.clear();
	}
	
	public void onError(TS3Error error, ErrorHandler handler) {
		if (!errorHandlers.containsKey(error)) errorHandlers.put(error, new HashSet<>());
		errorHandlers.get(error).add(handler);
	}
	
	private void channelChanged(Channel oldChannel, Channel newChannel) {
		if (oldChannel != null && channelCodecListener != null) // remove old channel codec listener, if it exists
			oldChannel.getProps().removeChangeListener(Channel.CodecUnencrypted.class, channelCodecListener);
		checkVoiceEncryption();
		channelCodecListener = newChannel.getProps().addChangeListener(Channel.CodecUnencrypted.class, b -> checkVoiceEncryption());
	}
	
	private void checkVoiceEncryption() {
		boolean voiceEncrypted = isVoiceEncrypted();
		Boolean outputAvailable = props.get(OutputAvailable.class);
		
		// update output status if it mismatches voice encryption status
		if (outputAvailable == null || outputAvailable == voiceEncrypted) {
			props.set(InputAvailable.class, !voiceEncrypted);
			props.set(OutputAvailable.class, !voiceEncrypted);
			props.flush();
			
			if (voiceEncrypted) { // TODO
				log.error(ansi().render("@|red Encrypted voice channels are not yet supported.|@").toString());
			}
		}
	}
	
	private boolean isVoiceEncrypted() {
		CodecEncryptionMode mode = server.getProps().get(Server.VoiceEncryptionMode.class);
		if (mode == null || mode == CodecEncryptionMode.GLOBALLY_ON) return true;
		
		Optional<Channel> optChannel = getChannel();
		if (!optChannel.isPresent()) return true;
		Channel channel = optChannel.get();
		
		boolean channelCodecUnencrypted = channel.getProps().get(Channel.CodecUnencrypted.class);
		return !channelCodecUnencrypted;
	}
	
	public void disconnect() {
		disconnect(0);
	}
	
	public void disconnect(int code) {
		if (disconnecting) return;
		disconnecting = true;
		
		if (commandHandler != null) {
			try {
				commandHandler.send(new CommandClientDisconnect(disconnectMessage));
			} catch (Exception ignored) {}
		}
		
		if (voiceHandler != null) {
			try {
				voiceHandler.dispose();
			} catch (Exception e) {
				log.error("Exception while trying to dispose voice handler", e);
			}
		}
		
		if (packetHandler != null) {
			try {
				packetHandler.disconnect();
				if (readLoopFuture != null) readLoopFuture.cancel(true);
			} catch (Exception e) {
				log.error("Exception while trying to stop packet handler", e);
			}
		}
		
		if (socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} catch (Exception e) {
				log.error("Exception while trying to disconnect", e);
			}
		}
		
		if (code != 0) {
			log.error(ansi().render("@|red Client disconnected with error code |@@|bold,red {}|@").toString(), code);
		}
		
		System.exit(code);
	}
	
	public void fatal(String error, Object... args) {
		log.error(ansi().render("@|bold,red " + error + "|@").toString(), args);
		disconnect(1);
	}
	
	@FunctionalInterface
	public interface ClientConnectedHandler {
		void handle(Client client);
	}
	
	@FunctionalInterface
	public interface ClientReadyHandler {
		void handle(Client client);
	}
	
	@FunctionalInterface
	public interface ErrorHandler {
		void handle(Client client, TS3Error error, CommandError command);
	}
	
	@RequiredArgsConstructor
	public static class FatalErrorHandler implements ErrorHandler {
		private final String error;
		
		@Override
		public void handle(Client client, TS3Error error, CommandError command) {
			client.fatal(this.error);
		}
	}
}
