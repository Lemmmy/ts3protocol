package pw.lemmmy.ts3protocol.packets.command;

import pw.lemmmy.ts3protocol.commands.Command;
import pw.lemmmy.ts3protocol.packets.PacketType;

public class PacketCommandLow extends PacketCommand {
	{
		packetType = PacketType.COMMAND_LOW;
	}
	
	public PacketCommandLow(Command command) {
		super(command);
	}
	
	public PacketCommandLow() {
		super();
	}
}
