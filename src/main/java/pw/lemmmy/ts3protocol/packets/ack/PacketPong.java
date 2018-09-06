package pw.lemmmy.ts3protocol.packets.ack;

import pw.lemmmy.ts3protocol.packets.PacketType;

public class PacketPong extends PacketAcknowledgement {
	{
		packetType = PacketType.PONG;
		unencrypted = true;
	}
	
	public PacketPong() {
		super();
	}
	
	public PacketPong(short packetID) {
		super(packetID);
	}
}
