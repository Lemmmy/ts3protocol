package pw.lemmmy.ts3protocol.voice.codecs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tomp2p.opuswrapper.Opus;

@AllArgsConstructor
@Getter
public enum CodecType {
	SPEEX_NARROWBAND(new SpeexCodec(8000, 1, 0)),
	SPEEX_WIDEBAND(new SpeexCodec(16000, 1, 1)),
	SPEEX_ULTRA_WIDEBAND(new SpeexCodec(32000, 1, 2)),
	CELT_MONO(null),
	OPUS_VOICE(new OpusCodec(48000, 1, Opus.OPUS_APPLICATION_VOIP)),
	OPUS_MUSIC(new OpusCodec(48000, 2, Opus.OPUS_APPLICATION_AUDIO));
	
	private VoiceCodec codec;
}
