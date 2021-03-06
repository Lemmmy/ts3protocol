package pw.lemmmy.ts3protocol.server;

import lombok.Getter;
import pw.lemmmy.ts3protocol.channels.Channel;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.channels.CommandChannelList;
import pw.lemmmy.ts3protocol.commands.channels.CommandNotifyChannelCreated;
import pw.lemmmy.ts3protocol.commands.channels.CommandNotifyChannelDeleted;
import pw.lemmmy.ts3protocol.commands.clients.CommandNotifyClientEnterView;
import pw.lemmmy.ts3protocol.commands.clients.CommandNotifyClientLeftView;
import pw.lemmmy.ts3protocol.commands.handshake.CommandInitServer;
import pw.lemmmy.ts3protocol.commands.server.CommandNotifyServerEdited;
import pw.lemmmy.ts3protocol.users.User;
import pw.lemmmy.ts3protocol.utils.properties.*;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Server {
	private Client client;
	
	private Map<Short, User> users = new HashMap<>();
	private Map<Short, Channel> channels = new HashMap<>();
	
	public final PropertyManager props;
	
	public Server(Client client) {
		this.client = client;
		
		props = new PropertyManager(client, null, CommandInitServer.class, CommandNotifyServerEdited.class);
		initialiseProperties();
		
		addCommandListeners();
	}
	
	@SuppressWarnings("unchecked")
	private void initialiseProperties() {
		props.add(
			new Name(), new PhoneticName(), new Nickname(), new WelcomeMessage(),
			new Platform(), new Version(),
			new ID(), new MaxClients(),
			new IP(), new Created(),
			new DefaultServerGroupID(), new DefaultChannelGroupID(),
			new DefaultTemporaryChannelDeleteDelay(),
			new VoiceEncryptionMode(),
			new PrioritySpeakerDimModifier(),
			new IconID(),
			new HostMessage(), new HostMessageMode(),
			new HostBannerURL(), new HostBannerGraphicsURL(), new HostBannerGraphicsInterval(), new HostBannerResizeMode(),
			new HostButtonTooltip(), new HostButtonURL(), new HostButtonGraphicsURL()
		);
	}
	
	private void addCommandListeners() {
		addElementListener(CommandNotifyClientEnterView.class, "clid", this::handleUserDiscovered);
		addElementListener(CommandNotifyClientLeftView.class, "clid", this::handleUserRemoved);
		
		addElementListener(CommandChannelList.class, "cid", this::handleChannelDiscovered);
		addElementListener(CommandNotifyChannelCreated.class, "cid", this::handleChannelDiscovered);
		addElementListener(CommandNotifyChannelDeleted.class, "cid", this::handleChannelRemoved);
	}
	
	// TODO: generify further to prevent duplication
	private void addElementListener(Class<? extends Command> commandClass, String idParameter, ElementDiscoveredHandler handler) {
		client.commandHandler.addCommandListener(commandClass, c -> c.getArgumentSets().forEach(args -> {
			if (!args.containsKey(idParameter)) return;
			short id = Short.parseShort(args.get(idParameter));
			handler.handle(c, args, id);
		}));
	}
	
	private void handleUserDiscovered(Command command, Map<String, String> args, short clientID) {
		if (!users.containsKey(clientID)) {
			User user = new User(client, this).setID(clientID);
			addUser(user);
			user.props.readFromArgumentSet(command, args);
		}
	}
	
	private void handleUserRemoved(Command command, Map<String, String> args, short clientID) {
		if (!users.containsKey(clientID)) return;
		users.remove(clientID);
	}
	
	public void addUser(User user) {
		users.put(user.getID(), user);
	}
	
	public User getUser(short id) {
		return users.get(id);
	}
	
	private void handleChannelDiscovered(Command command, Map<String, String> args, short channelID) {
		if (!channels.containsKey(channelID)) {
			Channel channel = new Channel(client, this).setID(channelID);
			addChannel(channel);
			channel.props.readFromArgumentSet(command, args);
		}
	}
	
	private void handleChannelRemoved(Command command, Map<String, String> args, short channelID) {
		if (!channels.containsKey(channelID)) return;
		channels.remove(channelID);
	}
	
	public void addChannel(Channel channel) {
		channels.put(channel.getID(), channel);
	}
	
	public Channel getChannel(short id) {
		return channels.get(id);
	}
	
	public class Name extends StringProperty {{ name = "virtualserver_name"; }}
	public class PhoneticName extends StringProperty {{ name = "virtualserver_name_phonetic"; }}
	public class Nickname extends StringProperty {{ name = "virtualserver_nickname"; }}
	public class WelcomeMessage extends StringProperty {{ name = "virtualserver_welcomemessage"; }}
	public class Platform extends StringProperty {{ name = "virtualserver_platform"; }}
	public class Version extends StringProperty {{ name = "virtualserver_version"; }}
	
	public class ID extends IntProperty {{ name = "virtualserver_id"; }}
	public class MaxClients extends IntProperty {{ name = "virtualserver_maxclients"; }}
	
	public class IP extends InetAddressListProperty {{ name = "virtualserver_ip"; }}
	public class Created extends DateProperty {{ name = "virtualserver_created"; }}
	
	public class DefaultServerGroupID extends IntProperty {{ name = "virtualserver_default_server_group"; }}
	public class DefaultChannelGroupID extends IntProperty {{ name = "virtualserver_default_channel_group"; }}
	
	public class DefaultTemporaryChannelDeleteDelay extends IntProperty {{ name = "virtualserver_channel_temp_delete_delay_default"; }}
	
	public class VoiceEncryptionMode extends EnumProperty<CodecEncryptionMode> {{ enumClass = CodecEncryptionMode.class; name = "virtualserver_codec_encryption_mode"; }}
	
	public class PrioritySpeakerDimModifier extends DoubleProperty {{ name = "virtualserver_priority_speaker_dimm_modificator"; }}
	
	public class IconID extends LongProperty {{ name = "virtualserver_icon_id"; }}
	
	public class HostMessage extends StringProperty {{ name = "virtualserver_hostmessage"; }}
	public class HostMessageMode extends EnumProperty<pw.lemmmy.ts3protocol.server.HostMessageMode> {
		{ enumClass = pw.lemmmy.ts3protocol.server.HostMessageMode.class; name = "virtualserver_hostmessage_mode"; }
	}
	
	public class HostBannerURL extends URLProperty {{ name = "virtualserver_hostbanner_url"; }}
	public class HostBannerGraphicsURL extends URLProperty {{ name = "virtualserver_hostbanner_gfx_url"; }}
	public class HostBannerGraphicsInterval extends IntProperty {{ name = "virtualserver_hostbanner_gfx_interval"; }}
	public class HostBannerResizeMode extends EnumProperty<pw.lemmmy.ts3protocol.server.HostBannerResizeMode> {
		{ enumClass = pw.lemmmy.ts3protocol.server.HostBannerResizeMode.class; name = "virtualserver_hostbanner_mode"; }
	}
	
	public class HostButtonTooltip extends StringProperty {{ name = "virtualserver_hostbutton_tooltip"; }}
	public class HostButtonURL extends URLProperty {{ name = "virtualserver_hostbutton_url"; }}
	public class HostButtonGraphicsURL extends URLProperty {{ name = "virtualserver_hostbutton_gfx_url"; }}
	
	@FunctionalInterface
	private interface ElementDiscoveredHandler {
		void handle(Command command, Map<String, String> args, short clientID);
	}
}
