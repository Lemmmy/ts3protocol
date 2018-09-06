package pw.lemmmy.ts3protocol.packets;

import lombok.AllArgsConstructor;
import pw.lemmmy.ts3protocol.Client;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.CommandRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class PacketCommand extends Packet {
	private Command command;
	
	{
		packetType = PacketType.COMMAND;
		unencrypted = false;
		compressed = true;
		newProtocol = true;
	}
	
	public PacketCommand() {}
	
	@Override
	protected void writeData(Client client, DataOutputStream os) throws IOException {
		System.out.println("C→S: " + command.encode());
		
		os.write(command.encode().getBytes());
	}
	
	@Override
	protected void readData(Client client, DataInputStream dis) {
		String data = new String(this.data, StandardCharsets.UTF_8); // TODO: ASCII or UTF-8?
		
		System.out.println("S→C: " + data);
		
		String[] args = data.split(" ");
		String commandName = args[0];
		
		command = CommandRegistry.getCommand(commandName).orElse(null);
		
		if (command == null) {
			System.err.println("Don't know how to handle command " + commandName);
		} else {
			command.decode(args);
			
			for (short packetID : packetIDs) {
				client.send(new PacketAck(packetID)); // lol
			}
			
			client.handleCommand(command);
		}
	}
}
