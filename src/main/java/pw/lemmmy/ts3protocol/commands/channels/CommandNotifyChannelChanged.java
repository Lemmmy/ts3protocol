package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

public class CommandNotifyChannelChanged extends CommandNotifyProperties {
	public CommandNotifyChannelChanged() {}
	
	@Override
	public String getName() {
		return "notifychannelchanged";
	}
}
