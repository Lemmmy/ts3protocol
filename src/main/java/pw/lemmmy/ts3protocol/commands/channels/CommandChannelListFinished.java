package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.Command;

public class CommandChannelListFinished extends Command {
	public CommandChannelListFinished() {}
	
	@Override
	public String getName() {
		return "channellistfinished";
	}
}
