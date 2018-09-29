package pw.lemmmy.ts3protocol.packets.command;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.commands.CommandRegistry;
import pw.lemmmy.ts3protocol.packets.Packet;
import pw.lemmmy.ts3protocol.packets.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.fusesource.jansi.Ansi.ansi;

@AllArgsConstructor
@Slf4j
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
		String encoded = command.encode();
		log.trace(ansi().render("@|green C→S|@: ") + encoded);
		os.write(encoded.getBytes());
	}
	
	@Override
	protected void readData(Client client, DataInputStream dis) {
		String data = new String(this.data, StandardCharsets.UTF_8);
		
		log.trace(ansi().render("@|cyan S→C|@: ") + data);
		
		String[] args = data.split(" ");
		String commandName = args[0];
		
		command = CommandRegistry.getCommand(commandName).orElse(null);
		
		if (command == null) {
			log.debug(ansi().render("@|red Don't know how to handle command |@@|bold,red {}|@").toString(), commandName);
		} else {
			command.decode(data.replaceFirst(commandName + " ", ""));
			client.commandHandler.handleCommand(command);
		}
	}
}
