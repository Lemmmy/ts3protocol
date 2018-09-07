package pw.lemmmy.ts3protocol.crypto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedKey {
	int generationID;
	byte[] key, nonce;
}
