package pw.lemmmy.ts3protocol.voice;

import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.packets.voice.PacketVoice;
import pw.lemmmy.ts3protocol.users.User;
import pw.lemmmy.ts3protocol.voice.codecs.CodecType;
import pw.lemmmy.ts3protocol.voice.codecs.VoiceCodec;

import java.util.HashSet;
import java.util.Set;

public class VoiceHandler {
	private Client client;
	
	private Set<VoiceListener> voiceListeners = new HashSet<>();
	
	public VoiceHandler(Client client) {
		this.client = client;
	}
	
	public void init() {
		for (CodecType codecType : CodecType.values()) {
			VoiceCodec codec = codecType.getCodec();
			if (codec != null) codec.init();
		}
	}
	
	public void addVoiceListener(VoiceListener listener) {
		voiceListeners.add(listener);
	}
	
	public void handleAudioPacket(PacketVoice voice) { // TODO: other audio packets + codecs
		try {
			CodecType codecType = voice.getCodecType();
			VoiceCodec codec = codecType.getCodec();
			if (codec == null) return;
			
			byte[] voiceData = voice.getVoiceData();
			if (voiceData.length <= 0) return;
			
			byte[] out = codec.decode(voiceData);
			if (out != null) {
				if (codec.getChannels() == 1) {
					out = VoiceUtils.monoToStereo(out);
				}
				final byte[] finalOut = out;
				
				User user = client.getServer().getUser(voice.getTalkingClient());
				voiceListeners.forEach(l -> {
					try {
						l.handle(user, finalOut, voice);
					} catch (Exception e) {
						System.err.println("Error in voice listener:");
						e.printStackTrace();
					}
				});
			}
		} catch (Exception e) {
			System.err.println("Error decoding voice packet:");
			e.printStackTrace();
		}
	}
	
	public void dispose() {
		for (CodecType codecType : CodecType.values()) {
			VoiceCodec codec = codecType.getCodec();
			if (codec != null) codec.dispose();
		}
	}
	
	@FunctionalInterface
	public interface VoiceListener {
		void handle(User user, byte[] decodedData, PacketVoice voice);
	}
}
