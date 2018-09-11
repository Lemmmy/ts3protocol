package pw.lemmmy.ts3protocol.commands.handshake;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;

@Getter
public class CommandInitServer extends CommandNotifyProperties {
	public CommandInitServer() {}
	
	@Override
	public String getName() {
		return "initserver";
	}
}
