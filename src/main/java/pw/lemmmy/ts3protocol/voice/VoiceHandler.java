package pw.lemmmy.ts3protocol.voice;

import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.packets.voice.PacketVoice;
import pw.lemmmy.ts3protocol.voice.codecs.CodecType;
import pw.lemmmy.ts3protocol.voice.codecs.VoiceCodec;

public class VoiceHandler {
	private Client client;
	
	public VoiceHandler(Client client) {
		this.client = client;
	}
	
	public void init() {
		for (CodecType codecType : CodecType.values()) {
			VoiceCodec codec = codecType.getCodec();
			if (codec != null) codec.init();
		}
	}
	
	public void handleAudioPacket(PacketVoice voice) { // TODO: other audio packets + codecs
		CodecType codecType = voice.getCodecType();
		VoiceCodec codec = codecType.getCodec();
		if (codec == null) return;
		
		byte[] out = codec.decode(voice.getVoiceData());
		if (out != null) {
			if (codec.getChannels() == 1) {
				out = VoiceUtils.monoToStereo(out);
			}
		}
	}
}
