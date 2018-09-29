package pw.lemmmy.ts3protocol.channels;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.channels.CommandChannelEdit;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;
import pw.lemmmy.ts3protocol.commands.properties.CommandUpdateProperties;
import pw.lemmmy.ts3protocol.utils.properties.PropertyManager;

import java.util.Map;

@Getter
@Slf4j
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
	
	@Override
	protected void handleFlushCommand(Command command) {
		super.handleFlushCommand(command);
		
		if (command instanceof CommandChannelEdit) {
			((CommandChannelEdit) command).setChannelID(channelID);
		} else {
			log.error("ChannelPropertyManager had wrong update command: {}", command.getClass().getName());
		}
	}
}
