package pw.lemmmy.ts3protocol.commands.clients;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

@Getter
public class CommandNotifyClientMoved extends CommandNotifyProperties {
	public CommandNotifyClientMoved() {}
	
	@Override
	public String getName() {
		return "notifyclientmoved";
	}
}
