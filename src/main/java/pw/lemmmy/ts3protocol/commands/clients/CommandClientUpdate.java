package pw.lemmmy.ts3protocol.commands.clients;

import pw.lemmmy.ts3protocol.commands.properties.CommandUpdateProperties;
import pw.lemmmy.ts3protocol.utils.properties.Property;

import java.util.List;

public class CommandClientUpdate extends CommandUpdateProperties {
	public CommandClientUpdate(List<Property<?>> properties) {
		super(properties);
	}
	
	public CommandClientUpdate() {}
	
	@Override
	public String getName() {
		return "clientupdate";
	}
}
