package pw.lemmmy.ts3protocol.commands.clients;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

@Getter
public class CommandNotifyClientEnterView extends CommandNotifyProperties {
	public CommandNotifyClientEnterView() {}
	
	@Override
	public String getName() {
		return "notifycliententerview";
	}
}
