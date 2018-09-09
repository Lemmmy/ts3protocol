package pw.lemmmy.ts3protocol.voice;

public class VoiceUtils {
	public static byte[] monoToStereo(byte[] pcm) {
		byte[] out = new byte[pcm.length * 2];
		
		for (int i = 0; i < pcm.length; i += 2) {
			out[i * 2 + 0] = pcm[i];
			out[i * 2 + 1] = pcm[i + 1];
			out[i * 2 + 2] = pcm[i];
			out[i * 2 + 3] = pcm[i + 1];
		}
		
		return out;
	}
}
