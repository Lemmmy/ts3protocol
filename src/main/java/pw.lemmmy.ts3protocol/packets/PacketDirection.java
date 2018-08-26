package pw.lemmmy.ts3protocol.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PacketDirection {
	CLIENT_TO_SERVER(13), SERVER_TO_CLIENT(11);
	
	private int headerSize;
	private int metaSize;
	
	PacketDirection(int headerSize) {
		this.headerSize = headerSize;
		this.metaSize = headerSize - LowLevelPacket.MAC_SIZE;
	}
}
