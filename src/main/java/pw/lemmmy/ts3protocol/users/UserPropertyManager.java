package pw.lemmmy.ts3protocol.users;

import lombok.Getter;
import lombok.Setter;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.properties.CommandNotifyProperties;
import pw.lemmmy.ts3protocol.commands.properties.CommandUpdateProperties;
import pw.lemmmy.ts3protocol.utils.properties.PropertyManager;

import java.util.Map;

@Getter
public class UserPropertyManager extends PropertyManager {
	@Setter private short clientID;
	
	public UserPropertyManager(short clientID,
							   Client client,
							   Class<? extends CommandUpdateProperties> updateCommand,
							   Class<? extends CommandNotifyProperties>... notifyHandlers) {
		super(client, updateCommand, notifyHandlers);
		
		this.clientID = clientID;
	}
	
	@Override
	protected boolean shouldReadCommand(Command command, Map<String, String> arguments) {
		return arguments.containsKey("clid") && Short.parseShort(arguments.get("clid")) == clientID;
	}
}
