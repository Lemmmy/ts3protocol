package pw.lemmmy.ts3protocol.commands.clients;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

@Getter
public class CommandNotifyClientLeftView extends CommandNotifyProperties {
	public CommandNotifyClientLeftView() {}
	
	@Override
	public String getName() {
		return "notifyclientleftview";
	}
}
