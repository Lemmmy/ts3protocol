package pw.lemmmy.ts3protocol.packets.ack;

import pw.lemmmy.ts3protocol.packets.PacketType;

public class PacketAck extends PacketAcknowledgement {
	{
		packetType = PacketType.ACK;
	}
	
	public PacketAck() {
		super();
	}
	
	public PacketAck(int packetID) {
		super(packetID);
	}
}
