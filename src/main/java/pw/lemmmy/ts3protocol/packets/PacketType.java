package pw.lemmmy.ts3protocol.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PacketType {
	VOICE(false, false),
	VOICE_WHISPER(false, false),
	COMMAND(true, true),
	COMMAND_LOW(true, true),
	PING(false, false),
	PONG(false, false),
	ACK(true, false),
	ACK_LOW(true, false),
	INIT_1(false, false);
	
	private boolean encrypted, fragmentable;
}