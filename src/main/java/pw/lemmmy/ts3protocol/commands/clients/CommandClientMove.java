package pw.lemmmy.ts3protocol.commands.clients;

import pw.lemmmy.ts3protocol.channels.Channel;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.users.User;

public class CommandClientMove extends Command {
	private short clientID, channelID;
	
	public CommandClientMove(User user, Channel channel) {
		this(user.getID(), channel.getID());
	}
	
	public CommandClientMove(short clientID, short channelID) {
		this.clientID = clientID;
		this.channelID = channelID;
	}
	
	public CommandClientMove() {}
	
	@Override
	public String getName() {
		return "clientmove";
	}
	
	@Override
	public void populateArguments() {
		arguments.put("clid", Short.toString(clientID));
		arguments.put("cid", Short.toString(channelID));
	}
}
