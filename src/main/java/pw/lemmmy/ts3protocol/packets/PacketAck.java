package pw.lemmmy.ts3protocol.packets;

import lombok.AllArgsConstructor;
import pw.lemmmy.ts3protocol.Client;

import java.io.DataOutputStream;
import java.io.IOException;

@AllArgsConstructor
public class PacketAck extends Packet {
	{
		packetType = PacketType.ACK;
		unencrypted = false;
	}
	
	private short packetID;
	
	public PacketAck() {}
	
	@Override
	protected void writeData(Client client, DataOutputStream os) throws IOException {
		os.writeShort(packetID);
	}
}
