package pw.lemmmy.ts3protocol.commands.errors;

import pw.lemmmy.ts3protocol.commands.Command;

public class CommandError extends Command {
	public CommandError() {}
	
	@Override
	public String getName() {
		return "error";
	}
}
