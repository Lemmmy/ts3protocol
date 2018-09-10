package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.Command;

public class CommandChannelSubscribeAll extends Command {
	public CommandChannelSubscribeAll() {}
	
	@Override
	public String getName() {
		return "channelsubscribeall";
	}
}
