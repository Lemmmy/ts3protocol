package pw.lemmmy.ts3protocol.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PacketType {
	VOICE(false, false, false),
	VOICE_WHISPER(false, false, false),
	COMMAND(true, true, true),
	COMMAND_LOW(true, true, true),
	PING(false, false, false),
	PONG(false, false, false),
	ACK(true, false, false),
	ACK_LOW(true, false, false),
	INIT_1(false, false, false);
	
	private boolean encrypted, compressible, fragmentable;
}