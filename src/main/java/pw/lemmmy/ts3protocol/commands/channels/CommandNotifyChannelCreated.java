package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

public class CommandNotifyChannelCreated extends CommandNotifyProperties {
	public CommandNotifyChannelCreated() {}
	
	@Override
	public String getName() {
		return "notifychannelcreated";
	}
}
