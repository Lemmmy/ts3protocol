package pw.lemmmy.ts3protocol.commands.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTargetMode {
	PRIVATE_MESSAGE(1), CHANNEL_MESSAGE(2), SERVER_MESSAGE(3);
	
	private int modeID;
}
