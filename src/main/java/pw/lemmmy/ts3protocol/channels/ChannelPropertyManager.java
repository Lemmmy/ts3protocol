package pw.lemmmy.ts3protocol.channels;

import lombok.Getter;
import lombok.Setter;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;
import pw.lemmmy.ts3protocol.commands.properties.CommandUpdateProperties;
import pw.lemmmy.ts3protocol.utils.properties.PropertyManager;

import java.util.Map;

@Getter
public class ChannelPropertyManager extends PropertyManager {
	@Setter private short channelID;
	
	public ChannelPropertyManager(short channelID,
								  Client client,
								  Class<? extends CommandUpdateProperties> updateCommand,
								  Class<? extends CommandNotifyProperties>... notifyHandlers) {
		super(client, updateCommand, notifyHandlers);
		
		this.channelID = channelID;
	}
	
	@Override
	protected boolean shouldReadCommand(Command command, Map<String, String> arguments) {
		return arguments.containsKey("cid") && Short.parseShort(arguments.get("cid")) == channelID;
	}
}
