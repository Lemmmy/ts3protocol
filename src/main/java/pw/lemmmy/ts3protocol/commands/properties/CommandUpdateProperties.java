package pw.lemmmy.ts3protocol.commands.properties;

import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.utils.properties.Property;

import java.util.List;

public abstract class CommandUpdateProperties extends Command {
	private List<Property<?>> properties;
	
	public CommandUpdateProperties(List<Property<?>> properties) {
		this.properties = properties;
	}
	
	public CommandUpdateProperties() {}
	
	@Override
	public void populateArguments() {
		properties.forEach(p -> p.encodeProperty(arguments));
	}
}
