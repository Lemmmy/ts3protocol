package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

public class CommandNotifyChannelEdited extends CommandNotifyProperties {
	public CommandNotifyChannelEdited() {}
	
	@Override
	public String getName() {
		return "notifychanneledited";
	}
}
