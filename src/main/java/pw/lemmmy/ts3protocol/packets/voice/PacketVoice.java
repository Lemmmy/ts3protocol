package pw.lemmmy.ts3protocol.packets.voice;

import lombok.Getter;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.packets.Packet;
import pw.lemmmy.ts3protocol.packets.PacketDirection;
import pw.lemmmy.ts3protocol.packets.PacketType;
import pw.lemmmy.ts3protocol.voice.codecs.CodecType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Getter
public class PacketVoice extends Packet {
	{
		packetType = PacketType.VOICE;
		unencrypted = false; // TODO
	}
	
	public short voicePacketID, talkingClient;
	public CodecType codecType;
	public byte[] voiceData;
	
	public PacketVoice() {}
	
	@Override
	protected void readData(Client client, DataInputStream dis) throws IOException {
		voicePacketID = dis.readShort();
		talkingClient = dis.readShort();
		
		codecType = CodecType.values()[dis.readByte()];
		
		voiceData = new byte[this.data.length - (direction == PacketDirection.SERVER_TO_CLIENT ? 5 : 3)];
		dis.read(voiceData);
		
		client.voiceHandler.handleAudioPacket(this);
	}
	
	@Override
	protected void writeData(Client client, DataOutputStream os) throws IOException {
		os.writeShort(voicePacketID);
		os.writeByte(codecType.ordinal());
		os.write(voiceData);
	}
}
