package pw.lemmmy.ts3protocol.voice.codecs;

import org.xiph.speex.SpeexDecoder;

import java.io.StreamCorruptedException;

public class SpeexCodec extends VoiceCodec {
	private SpeexDecoder decoder;
	
	private int mode;
	
	public SpeexCodec(int sampleRate, int channels, int mode) {
		super(sampleRate, channels);
		
		this.mode = mode;
	}
	
	@Override
	public void init() {
		decoder = new SpeexDecoder();
		decoder.init(mode, getSampleRate(), getChannels(), false);
	}
	
	@Override
	public byte[] decode(byte[] data) {
		try {
			decoder.processData(data, 0, data.length);
			byte[] out = new byte[decoder.getProcessedDataByteSize()];
			decoder.getProcessedData(out, 0);
			return out;
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		}
		
		return new byte[0];
	}
	
	@Override
	public byte[] encode(byte[] data) {
		return new byte[0];
	}
}
