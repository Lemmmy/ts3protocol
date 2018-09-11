package pw.lemmmy.ts3protocol.voice.codecs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class VoiceCodec {
	protected final int sampleRate, channels;
	
	public abstract void init();
	
	/**
	 * Decodes a voice packet.
	 * @param data The raw data from the voice packet.
	 * @return The decoded voice data, as signed 16-bit PCM, stereo.
	 */
	public abstract byte[] decode(byte[] data);
	
	/**
	 * Encodes a voice packet.
	 * @param data The input data, as signed 16-bit PCM, stereo.
	 * @return The encoded voice data.
	 */
	public abstract byte[] encode(byte[] data);
}
