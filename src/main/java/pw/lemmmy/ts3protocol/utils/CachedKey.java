package pw.lemmmy.ts3protocol.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedKey {
	int generationID;
	byte[] key, nonce;
}
