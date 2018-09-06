package pw.lemmmy.ts3protocol.commands.handshake;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.Command;

@Getter
public class CommandInitServer extends Command {
	public CommandInitServer() {}
	
	@Override
	public String getName() {
		return "initserver";
	}
}
