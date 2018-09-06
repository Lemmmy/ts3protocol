package pw.lemmmy.ts3protocol.commands;

public class CommandSendTextMessage extends Command {
	private int targetMode; // TODO: create a Channel type
	private String message;
	
	public CommandSendTextMessage(int targetMode, String message) {
		this.targetMode = targetMode;
		this.message = message;
	}
	
	public CommandSendTextMessage() {}
	
	@Override
	public String getName() {
		return "sendtextmessage";
	}
	
	@Override
	public void populateArguments() {
		arguments.put("targetmode", Integer.toString(targetMode));
		arguments.put("msg", message);
	}
}
