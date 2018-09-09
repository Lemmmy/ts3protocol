package pw.lemmmy.ts3protocol.server;

import lombok.Getter;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.handshake.CommandInitServer;
import pw.lemmmy.ts3protocol.commands.server.CommandNotifyServerEdited;
import pw.lemmmy.ts3protocol.utils.properties.*;

@Getter
public class Server {
	private Client client;
	
	private PropertyManager properties;
	
	public Server(Client client) {
		this.client = client;
		
		initialiseProperties(client);
	}
	
	private void initialiseProperties(Client client) {
		properties = new PropertyManager(client, null, CommandInitServer.class, CommandNotifyServerEdited.class);
		
		properties.add(
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
	
	public class Name extends StringProperty { public String getName() { return "virtualserver_name"; } }
	public class PhoneticName extends StringProperty { public String getName() { return "virtualserver_name_phonetic"; } }
	public class Nickname extends StringProperty { public String getName() { return "virtualserver_nickname"; } }
	public class WelcomeMessage extends StringProperty { public String getName() { return "virtualserver_welcomemessage"; } }
	public class Platform extends StringProperty { public String getName() { return "virtualserver_platform"; } }
	public class Version extends StringProperty { public String getName() { return "virtualserver_version"; } }
	
	public class ID extends IntProperty { public String getName() { return "virtualserver_id"; } }
	public class MaxClients extends IntProperty { public String getName() { return "virtualserver_maxclients"; } }
	
	public class IP extends InetAddressProperty { public String getName() { return "virtualserver_ip"; } }
	public class Created extends DateProperty { public String getName() { return "virtualserver_created"; } }
	
	public class DefaultServerGroupID extends IntProperty { public String getName() { return "virtualserver_default_server_group"; } }
	public class DefaultChannelGroupID extends IntProperty { public String getName() { return "virtualserver_default_channel_group"; } }
	
	public class DefaultTemporaryChannelDeleteDelay extends IntProperty { public String getName() { return "virtualserver_channel_temp_delete_delay_default"; } }
	
	public class VoiceEncryptionMode extends EnumProperty<CodecEncryptionMode> {
		public Class<CodecEncryptionMode> getEnumClass() { return CodecEncryptionMode.class; }
		public String getName() { return "virtualserver_created"; }
	}
	
	public class PrioritySpeakerDimModifier extends DoubleProperty { public String getName() { return "virtualserver_priority_speaker_dimm_modificator"; } }
	
	public class IconID extends LongProperty { public String getName() { return "virtualserver_icon_id"; } }
	
	public class HostMessage extends StringProperty { public String getName() { return "virtualserver_hostmessage"; } }
	public class HostMessageMode extends EnumProperty<pw.lemmmy.ts3protocol.server.HostMessageMode> {
		public Class<pw.lemmmy.ts3protocol.server.HostMessageMode> getEnumClass() { return pw.lemmmy.ts3protocol.server.HostMessageMode.class; }
		public String getName() { return "virtualserver_hostmessage_mode"; }
	}
	
	public class HostBannerURL extends URLProperty { public String getName() { return "virtualserver_hostbanner_url"; } }
	public class HostBannerGraphicsURL extends URLProperty { public String getName() { return "virtualserver_hostbanner_gfx_url"; } }
	public class HostBannerGraphicsInterval extends IntProperty { public String getName() { return "virtualserver_hostbanner_gfx_interval"; } }
	public class HostBannerResizeMode extends EnumProperty<pw.lemmmy.ts3protocol.server.HostBannerResizeMode> {
		public Class<pw.lemmmy.ts3protocol.server.HostBannerResizeMode> getEnumClass() { return pw.lemmmy.ts3protocol.server.HostBannerResizeMode.class; }
		public String getName() { return "virtualserver_hostbanner_mode"; }
	}
	
	public class HostButtonTooltip extends StringProperty { public String getName() { return "virtualserver_hostbutton_tooltip"; } }
	public class HostButtonURL extends URLProperty { public String getName() { return "virtualserver_hostbutton_url"; } }
	public class HostButtonGraphicsURL extends URLProperty { public String getName() { return "virtualserver_hostbutton_gfx_url"; } }
}
