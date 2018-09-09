package pw.lemmmy.ts3protocol.users;

import lombok.Getter;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.clients.CommandClientUpdate;
import pw.lemmmy.ts3protocol.commands.clients.CommandNotifyClientEnterView;
import pw.lemmmy.ts3protocol.commands.clients.CommandNotifyClientUpdated;
import pw.lemmmy.ts3protocol.server.Server;
import pw.lemmmy.ts3protocol.utils.properties.IntProperty;
import pw.lemmmy.ts3protocol.utils.properties.StringProperty;

@Getter
public class User {
	private Client client;
	protected Server server;
	public UserPropertyManager props;
	
	private short id = 0;
	
	public User() {}
	
	public User(Client client, Server server) {
		this.server = server;
		setClient(client);
	}
	
	public void setClient(Client client) {
		this.client = client;
		
		System.out.println("Setting client");
		
		props = new UserPropertyManager(id, client, CommandClientUpdate.class, CommandNotifyClientEnterView.class, CommandNotifyClientUpdated.class);
		initialiseProperties();
	}
	
	private void initialiseProperties() {
		System.out.println("Initialising properties");
		props.add(
			new Nickname(), new PhoneticNickname(),
			new ID()
		);
		
		props.addChangeListener(ID.class, p -> setID(p.getValue().shortValue()));
	}
	
	public short getID() {
		return id;
	}
	
	public void setID(short id) {
		this.id = id;
		props.setClientID(id);
	}
	
	public class Nickname extends StringProperty {{ name = "client_nickname"; }}
	public class PhoneticNickname extends StringProperty {{ name = "client_nickname_phonetic"; }}
	
	public class ID extends IntProperty {{ name = "clid"; }}
}
