package pw.lemmmy.ts3protocol.voice;

import pw.lemmmy.ts3protocol.client.Client;
import pw.lemmmy.ts3protocol.packets.voice.PacketVoice;
import pw.lemmmy.ts3protocol.users.User;
import pw.lemmmy.ts3protocol.voice.codecs.CodecType;
import pw.lemmmy.ts3protocol.voice.codecs.VoiceCodec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VoiceHandler {
	private Client client;
	
	private Set<VoiceListener> voiceListeners = new HashSet<>();
	private Map<Short, Short> lastPacketIDs = new HashMap<>();
	
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
			short talkingClient = voice.getTalkingClient();
			short voicePacketID = voice.getVoicePacketID();
			
			int packetsLost = Math.min(lastPacketIDs.containsKey(talkingClient) ? voicePacketID - lastPacketIDs.get(talkingClient) - 1 : 0, 5);
			lastPacketIDs.put(talkingClient, voicePacketID);
			
			CodecType codecType = voice.getCodecType();
			VoiceCodec codec = codecType.getCodec();
			if (codec == null) return;
			
			byte[] voiceData = voice.getVoiceData();
			if (voiceData.length <= 0) return;
			
			User user = client.getServer().getUser(talkingClient);
			
			for (int i = 0; i < packetsLost; i++) {
				byte[] out = codec.decode(null);
				if (out != null) {
					if (codec.getChannels() == 1) out = VoiceUtils.monoToStereo(out);
					handleVoiceListeners(user, out, voice);
				}
			}
			
			byte[] out = codec.decode(voiceData);
			if (out != null) {
				if (codec.getChannels() == 1) out = VoiceUtils.monoToStereo(out);
				handleVoiceListeners(user, out, voice);
			}
		} catch (Exception e) {
			System.err.println("Error decoding voice packet:");
			e.printStackTrace();
		}
	}
	
	private void handleVoiceListeners(User user, byte[] out, PacketVoice voice) {
		voiceListeners.forEach(l -> {
			try {
				l.handle(user, out, voice);
			} catch (Exception e) {
				System.err.println("Error in voice listener:");
				e.printStackTrace();
			}
		});
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
