package pw.lemmmy.ts3protocol.server;

import lombok.Getter;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.CommandHandler;
import pw.lemmmy.ts3protocol.commands.handshake.CommandInitServer;
import pw.lemmmy.ts3protocol.commands.server.CommandNotifyServerEdited;
import pw.lemmmy.ts3protocol.utils.properties.*;

@Getter
public class Server {
	private Client client;
	private CommandHandler commandHandler;
	
	private PropertyManager properties;
	
	/**
	 * Referred to internally as 'dimm modificator' - how much to lower the client volume by when the priority speaker is talking, in dB
	 */
	private int prioritySpeakerDuckModifier;
	
	public Server(Client client) {
		this.client = client;
		this.commandHandler = client.getCommandHandler();
		
		initialiseProperties(client);
		addCommandListeners();
	}
	
	protected void addCommandListeners() {
		commandHandler.addCommandListener(CommandInitServer.class, this::handleInitServer);
	}
	
	private void initialiseProperties(Client client) {
		properties = new PropertyManager(client, null, CommandNotifyServerEdited.class);
		
		properties.add(new Name());
		properties.add(new PhoneticName());
		properties.add(new WelcomeMessage());
		properties.add(new Platform());
		properties.add(new Version());
	}
	
	private void handleInitServer(CommandInitServer initServer) {
		properties.readFromCommand(initServer);
	}
	
	public class Name extends StringProperty { public String getName() { return "virtualserver_name"; } }
	public class PhoneticName extends StringProperty { public String getName() { return "virtualserver_name_phonetic"; } }
	public class WelcomeMessage extends StringProperty { public String getName() { return "virtualserver_welcomemessage"; } }
	public class Platform extends StringProperty { public String getName() { return "virtualserver_platform"; } }
	public class Version extends StringProperty { public String getName() { return "virtualserver_version"; } }
	
	public class MaxClients extends IntProperty { public String getName() { return "virtualserver_maxclients"; } }
	
	public class Created extends DateProperty { public String getName() { return "virtualserver_created"; } }
	
	public class VoiceEncryptionMode extends EnumProperty<CodecEncryptionMode> {
		public Class<CodecEncryptionMode> getEnumClass() { return CodecEncryptionMode.class; }
		public String getName() { return "virtualserver_created"; }
	}
}
