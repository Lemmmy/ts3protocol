package pw.lemmmy.ts3protocol.commands.clients;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

@Getter
public class CommandNotifyClientUpdated extends CommandNotifyProperties {
	public CommandNotifyClientUpdated() {}
	
	@Override
	public String getName() {
		return "notifyclientupdated";
	}
}
