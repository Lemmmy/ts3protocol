package pw.lemmmy.ts3protocol.packets.ack;

import lombok.AllArgsConstructor;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.packets.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@AllArgsConstructor
public abstract class PacketAcknowledgement extends Packet {
	{
		unencrypted = false;
	}
	
	private short packetID;
	
	public PacketAcknowledgement() {}
	
	@Override
	protected void writeData(Client client, DataOutputStream os) throws IOException {
		os.writeShort(packetID);
	}
	
	@Override
	protected void readData(Client client, DataInputStream dis) throws IOException {
		packetID = dis.readShort();
	}
}
