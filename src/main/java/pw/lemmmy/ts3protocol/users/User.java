package pw.lemmmy.ts3protocol.users;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pw.lemmmy.ts3protocol.channels.Channel;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.clients.CommandClientUpdate;
import pw.lemmmy.ts3protocol.commands.clients.CommandNotifyClientEnterView;
import pw.lemmmy.ts3protocol.commands.clients.CommandNotifyClientMoved;
import pw.lemmmy.ts3protocol.commands.clients.CommandNotifyClientUpdated;
import pw.lemmmy.ts3protocol.server.Server;
import pw.lemmmy.ts3protocol.utils.properties.*;

import java.util.Optional;

@Getter
public class User {
	protected Server server;
	public UserPropertyManager props;
	
	private short id = 0;
	
	public User() {}
	
	public User(Client client, Server server) {
		this.server = server;
		setClient(client);
	}
	
	@SuppressWarnings("unchecked")
	public void setClient(Client client) {
		props = new UserPropertyManager(
			id, client,
			CommandClientUpdate.class, CommandNotifyClientEnterView.class, CommandNotifyClientUpdated.class,
			CommandNotifyClientMoved.class
		);
		initialiseProperties();
	}
	
	private void initialiseProperties() {
		props.add(
			new Nickname(), new PhoneticNickname(),
			new ID(), new UUID(), new DatabaseID(), new MyTeamspeakID(),
			new Description(), new Country(), new AvatarID(), new IconID(), new Badges(), new Integrations(), new Metadata(),
			new ChannelID(),
			new InputMuted(), new OutputMuted(), new OutputOnlyMuted(), new InputAvailable(), new OutputAvailable(),
			new Away(), new AwayMessage(),
			new UnreadMessages(),
			new Recording(), new Talker(), new PrioritySpeaker(), new ChannelCommander(),
			new TalkPower(), new RequestingTalk(), new TalkRequestMessage(),
			new ServerQuery(), new NeededServerQueryViewPower(),
			new ServerGroups(), new ChannelGroupID(), new ChannelGroupInheritedID()
		);
		
		props.addChangeListener(ID.class, p -> setID(p.getValue().shortValue()));
	}
	
	public short getID() {
		return id;
	}
	
	public User setID(short id) {
		this.id = id;
		props.setClientID(id);
		return this;
	}
	
	public Optional<Channel> getChannel() {
		if (props == null || props.get(ChannelID.class) == null) return Optional.empty();
		short channelID = props.get(ChannelID.class);
		if (channelID <= 0) return Optional.empty();
		return Optional.ofNullable(server.getChannel(channelID));
	}
	
	public class Nickname extends StringProperty {{ name = "client_nickname"; }}
	public class PhoneticNickname extends StringProperty {{ name = "client_nickname_phonetic"; }}
	
	public class ID extends IntProperty {{ name = "clid"; }}
	// this is possibly the base64 of their identity's public key?
	public class UUID extends StringProperty {{ name = "client_unique_identifier"; }}
	public class DatabaseID extends IntProperty {{ name = "client_database_id"; }}
	public class MyTeamspeakID extends StringProperty {{ name = "client_myteamspeak_id"; }}
	
	public class ChannelID extends ShortProperty {{ name = "ctid"; fromRootSet = true; }}
	
	public class Description extends StringProperty {{ name = "client_description"; }}
	public class Country extends StringProperty {{ name = "client_country"; }}
	public class AvatarID extends StringProperty {{ name = "client_flag_avatar"; }}
	public class IconID extends LongProperty {{ name = "client_icon_id"; }}
	public class Badges extends StringProperty {{ name = "client_badges"; }}
	public class Integrations extends StringProperty {{ name = "client_integrations"; }}
	public class Metadata extends StringProperty {{ name = "client_meta_data"; }}
	
	public class InputMuted extends BooleanProperty  {{ name = "client_input_muted"; }}
	public class OutputMuted extends BooleanProperty  {{ name = "client_output_muted"; }}
	public class OutputOnlyMuted extends BooleanProperty  {{ name = "client_outputonly_muted"; }}
	public class InputAvailable extends BooleanProperty  {{ name = "client_input_hardware"; }}
	public class OutputAvailable extends BooleanProperty  {{ name = "client_output_hardware"; }}
	
	public class Away extends BooleanProperty  {{ name = "client_away"; }}
	public class AwayMessage extends StringProperty  {{ name = "client_away_message"; }}
	
	public class UnreadMessages extends IntProperty {{ name = "client_unread_messages"; }}
	
	public class Recording extends BooleanProperty  {{ name = "client_is_recording"; }}
	public class Talker extends BooleanProperty  {{ name = "client_is_talker"; }}
	public class PrioritySpeaker extends BooleanProperty  {{ name = "client_is_priority_speaker"; }}
	public class ChannelCommander extends BooleanProperty  {{ name = "client_is_channel_commander"; }}
	
	public class TalkPower extends IntProperty  {{ name = "client_talk_power"; }}
	public class RequestingTalk extends BooleanProperty  {{ name = "client_talk_request"; }}
	public class TalkRequestMessage extends StringProperty  {{ name = "client_talk_request_message"; }}
	
	public class ServerQuery extends BooleanProperty  {{ name = "client_type"; }}
	public class NeededServerQueryViewPower extends IntProperty  {{ name = "client_needed_serverquery_view_power"; }}
	
	// TODO: this is CSV; make a CSV type
	public class ServerGroups extends StringProperty  {{ name = "client_servergroups"; }}
	public class ChannelGroupID extends IntProperty  {{ name = "client_channel_group_id"; }}
	public class ChannelGroupInheritedID extends IntProperty  {{ name = "client_channel_group_inherited_channel_id"; }}
}
