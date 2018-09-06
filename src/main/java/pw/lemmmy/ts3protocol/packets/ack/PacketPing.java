package pw.lemmmy.ts3protocol.packets.ack;

import pw.lemmmy.ts3protocol.packets.Packet;
import pw.lemmmy.ts3protocol.packets.PacketType;

public class PacketPing extends Packet {
	{
		packetType = PacketType.PING;
		unencrypted = true;
	}
	
	public PacketPing() {}
}
