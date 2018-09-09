package pw.lemmmy.ts3protocol.packets.voice;

import lombok.Getter;
import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.packets.Packet;
import pw.lemmmy.ts3protocol.packets.PacketDirection;
import pw.lemmmy.ts3protocol.packets.PacketType;
import pw.lemmmy.ts3protocol.voice.codecs.CodecType;

import java.io.DataInputStream;
import java.io.IOException;

@Getter
public class PacketVoice extends Packet {
	{
		packetType = PacketType.VOICE;
		unencrypted = false; // TODO
	}
	
	private short voicePacketID, talkingClient;
	private CodecType codecType;
	private byte[] voiceData;
	
	public PacketVoice() {}
	
	@Override
	protected void readData(Client client, DataInputStream dis) throws IOException {
		voicePacketID = dis.readShort();
		talkingClient = dis.readShort();
		
		codecType = CodecType.values()[dis.readByte()];
		
		voiceData = new byte[this.data.length - (direction == PacketDirection.SERVER_TO_CLIENT ? 5 : 3)];
		dis.read(voiceData);
		
		client.getVoiceHandler().handleAudioPacket(this);
	}
}
