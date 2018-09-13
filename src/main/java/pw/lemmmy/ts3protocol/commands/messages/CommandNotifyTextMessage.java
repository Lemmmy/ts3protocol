package pw.lemmmy.ts3protocol.commands.messages;

import pw.lemmmy.ts3protocol.commands.Command;

public class CommandNotifyTextMessage extends Command {
	private MessageTargetMode targetMode;
	private int invokerID;
	private String message, invokerName, invokerUID;
	
	public CommandNotifyTextMessage() {}
	
	@Override
	public String getName() {
		return "notifytextmessage";
	}
}
