package pw.lemmmy.ts3protocol.commands;

public class CommandClientDisconnect extends Command {
	private int reasonID;
	private String reasonMessage;
	
	public CommandClientDisconnect() {}
	
	public CommandClientDisconnect(String reasonMessage) {
		this(0, reasonMessage);
	}
	
	public CommandClientDisconnect(int reasonID, String reasonMessage) {
		this.reasonID = reasonID;
		this.reasonMessage = reasonMessage;
	}
	
	@Override
	public String getName() {
		return "clientdisconnect";
	}
	
	@Override
	public void populateArguments() {
		arguments.put("reasonid", Integer.toString(reasonID));
		arguments.put("reasonmsg", reasonMessage);
	}
}
