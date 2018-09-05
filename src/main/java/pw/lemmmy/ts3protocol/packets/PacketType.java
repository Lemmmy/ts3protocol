package pw.lemmmy.ts3protocol.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PacketType {
	VOICE(false),
	VOICE_WHISPER(false),
	COMMAND(true),
	COMMAND_LOW(true),
	PING(false),
	PONG(false),
	ACK(true),
	ACK_LOW(true),
	INIT_1(false);
	
	private boolean encrypted;
}