package pw.lemmmy.ts3protocol.commands.messages;

import pw.lemmmy.ts3protocol.commands.Command;

public class CommandNotifyTextMessage extends Command {
	private int targetMode, invokerID; // TODO: create a Channel type
	private String message, invokerName, invokerUID;
	
	public CommandNotifyTextMessage() {}
	
	@Override
	public String getName() {
		return "notifytextmessage";
	}
}
