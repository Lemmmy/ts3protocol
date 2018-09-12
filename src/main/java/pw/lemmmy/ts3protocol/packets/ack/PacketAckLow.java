package pw.lemmmy.ts3protocol.packets.ack;

import pw.lemmmy.ts3protocol.packets.PacketType;

public class PacketAckLow extends PacketAcknowledgement {
	{
		packetType = PacketType.ACK_LOW;
	}
	
	public PacketAckLow() {
		super();
	}
	
	public PacketAckLow(int packetID) {
		super(packetID);
	}
}
