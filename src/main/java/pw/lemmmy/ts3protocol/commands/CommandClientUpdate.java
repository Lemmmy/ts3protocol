package pw.lemmmy.ts3protocol.commands;

public class CommandClientUpdate extends Command {
	private String parameter, value;
	
	public CommandClientUpdate(String parameter, String value) {
		this.parameter = parameter;
		this.value = value;
	}
	
	public CommandClientUpdate() {}
	
	@Override
	public String getName() {
		return "clientupdate";
	}
	
	@Override
	public void populateArguments() {
		arguments.put(parameter, value);
	}
}
