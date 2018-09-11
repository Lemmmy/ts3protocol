package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

public class CommandNotifyChannelDeleted extends CommandNotifyProperties {
	public CommandNotifyChannelDeleted() {}
	
	@Override
	public String getName() {
		return "notifychanneldeleted";
	}
}
