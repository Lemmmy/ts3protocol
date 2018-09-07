package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.Command;

public class CommandChannelList extends Command {
	public CommandChannelList() {}
	
	@Override
	public String getName() {
		return "channellist";
	}
	
	@Override
	public void decode(String data) {
		super.decode(data);
		
		argumentSets.forEach(set -> {
			System.out.println("Discovered channel: " + set.get("channel_name"));
		});
	}
}
