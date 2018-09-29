package pw.lemmmy.ts3protocol.commands.channels;

import pw.lemmmy.ts3protocol.commands.properties.CommandUpdateProperties;
import pw.lemmmy.ts3protocol.utils.properties.Property;

import java.util.List;

public class CommandChannelEdit extends CommandUpdateProperties {
	public CommandChannelEdit(List<Property<?>> properties) {
		super(properties);
	}
	
	public CommandChannelEdit() {}
	
	@Override
	public String getName() {
		return "channeledit";
	}
}
