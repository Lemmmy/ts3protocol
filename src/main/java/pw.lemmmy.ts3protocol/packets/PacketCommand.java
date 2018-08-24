package pw.lemmmy.ts3protocol.packets;

import pw.lemmmy.ts3protocol.commands.Command;

import java.io.DataOutputStream;
import java.io.IOException;

public class PacketCommand extends Packet {
	private Command command;
	
	{
		packetType = PacketType.COMMAND;
	}
	
	public PacketCommand() {}
	
	public PacketCommand(Command command) {
		this.command = command;
		this.unencrypted = false;
	}
	
	@Override
	protected void writeData(DataOutputStream os) throws IOException {
		os.write(command.encode().getBytes());
	}
}
