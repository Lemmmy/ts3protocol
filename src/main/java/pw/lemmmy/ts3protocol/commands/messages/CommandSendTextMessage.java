package pw.lemmmy.ts3protocol.commands.messages;

import pw.lemmmy.ts3protocol.commands.Command;

public class CommandSendTextMessage extends Command {
	private MessageTargetMode targetMode;
	private int target;
	private String message;
	
	public CommandSendTextMessage(MessageTargetMode targetMode, String message) {
		this(targetMode, -1, message);
	}
	
	public CommandSendTextMessage(MessageTargetMode targetMode, int target, String message) {
		this.targetMode = targetMode;
		this.target = target;
		this.message = message;
	}
	
	public CommandSendTextMessage() {}
	
	@Override
	public String getName() {
		return "sendtextmessage";
	}
	
	@Override
	public void populateArguments() {
		arguments.put("targetmode", Integer.toString(targetMode.getModeID()));
		if (target != -1) arguments.put("target", Integer.toString(target));
		arguments.put("msg", message);
	}
}
