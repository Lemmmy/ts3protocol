package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

public class CommandNotifyChannelDescriptionChanged extends CommandNotifyProperties {
	public CommandNotifyChannelDescriptionChanged() {}
	
	@Override
	public String getName() {
		return "notifychanneldescriptionchanged";
	}
}
