package pw.lemmmy.ts3protocol.commands.handshake;

import lombok.Getter;
import pw.lemmmy.ts3protocol.commands.Command;

@Getter
public class CommandInitIVExpand2 extends Command {
	public CommandInitIVExpand2() {}
	
	@Override
	public String getName() {
		return "initivexpand2";
	}
}
