package pw.lemmmy.ts3protocol.channels;

import lombok.Getter;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.channels.CommandNotifyChannelChanged;
import pw.lemmmy.ts3protocol.commands.channels.CommandNotifyChannelEdited;
import pw.lemmmy.ts3protocol.server.Server;
import pw.lemmmy.ts3protocol.utils.properties.*;
import pw.lemmmy.ts3protocol.voice.codecs.CodecType;

import java.util.Optional;

@Getter
public class Channel {
	private Server server;
	public ChannelPropertyManager props;
	
	private short id = 0;
	
	@SuppressWarnings("unchecked")
	public Channel(Client client, Server server) {
		this.server = server;
		
		props = new ChannelPropertyManager(
			id, client, null,
			CommandNotifyChannelChanged.class, CommandNotifyChannelEdited.class
		);
		initialiseProperties();
	}
	
	private void initialiseProperties() {
		props.add(
			new Name(), new PhoneticName(), new Topic(),
			new ID(), new ParentID(), new Order(), new IconID(),
			new MaxClients(), new MaxFamilyClients(),
			new MaxClientsUnlimited(), new MaxFamilyClientsUnlimited(), new MaxFamilyClientsInherited(),
			new Codec(), new CodecQuality(), new CodecUnencrypted(),
			new Permanent(), new SemiPermanent(), new Default(), new HasPassword(), new Private(),
			new DeleteDelay(),
			new NeededTalkPower(), new ForcedSilence()
		);
	}
	
	public short getID() {
		return id;
	}
	
	public Channel setID(short id) {
		this.id = id;
		props.setChannelID(id);
		return this;
	}
	
	public Optional<Channel> getParent() {
		if (props == null || props.get(ParentID.class) == null) return Optional.empty();
		short parentID = props.get(ParentID.class);
		if (parentID <= 0) return Optional.empty();
		return Optional.ofNullable(server.getChannel(parentID));
	}
	
	public class Name extends StringProperty {{ name = "channel_name"; }}
	public class PhoneticName extends StringProperty {{ name = "channel_name_phonetic"; }}
	public class Topic extends StringProperty {{ name = "channel_topic"; }}
	
	public class ID extends ShortProperty {{ name = "cid"; }}
	public class ParentID extends ShortProperty {{ name = "cpid"; }}
	public class Order extends IntProperty {{ name = "channel_order"; }}
	public class IconID extends LongProperty {{ name = "channel_icon_id"; }}
	
	public class MaxClients extends IntProperty {{ name = "channel_maxclients"; }}
	public class MaxFamilyClients extends IntProperty {{ name = "channel_maxfamilyclients"; }}
	public class MaxClientsUnlimited extends BooleanProperty {{ name = "channel_maxclients_unlimited"; }}
	public class MaxFamilyClientsUnlimited extends BooleanProperty {{ name = "channel_maxfamilyclients_unlimited"; }}
	public class MaxFamilyClientsInherited extends BooleanProperty {{ name = "channel_maxfamilyclients_inherited"; }}
	
	public class Codec extends EnumProperty<CodecType> {{ enumClass = CodecType.class; name = "channel_codec"; }}
	public class CodecQuality extends IntProperty {{ name = "channel_codec_quality"; }}
	public class CodecUnencrypted extends BooleanProperty {{ name = "channel_codec_is_unencrypted"; }}
	
	public class Permanent extends BooleanProperty {{ name = "channel_flag_permanent"; }}
	public class SemiPermanent extends BooleanProperty {{ name = "channel_flag_semi_permanent"; }}
	public class Default extends BooleanProperty {{ name = "channel_flag_default"; }}
	public class HasPassword extends BooleanProperty {{ name = "channel_flag_password"; }}
	public class Private extends BooleanProperty {{ name = "channel_flag_private"; }}
	
	/**
	 * Delay to automatically delete a temporary channel, in seconds
	 */
	public class DeleteDelay extends IntProperty {{ name = "channel_delete_delay"; }}
	
	public class NeededTalkPower extends IntProperty {{ name = "channel_needed_talk_power"; }}
	public class ForcedSilence extends BooleanProperty {{ name = "channel_forced_silence"; }}
}
