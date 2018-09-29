package pw.lemmmy.ts3protocol.commands.channels;

import lombok.Setter;
import pw.lemmmy.ts3protocol.commands.properties.CommandUpdateProperties;
import pw.lemmmy.ts3protocol.utils.properties.Property;

import java.util.List;

public class CommandChannelEdit extends CommandUpdateProperties {
	@Setter private short channelID;
	
	public CommandChannelEdit(List<Property<?>> properties) {
		super(properties);
	}
	
	public CommandChannelEdit() {}
	
	@Override
	public String getName() {
		return "channeledit";
	}
	
	@Override
	public void populateArguments() {
		super.populateArguments();
		
		arguments.put("cid", Short.toString(channelID));
	}
}
