package pw.lemmmy.ts3protocol.voice.codecs;

import org.xiph.speex.SpeexDecoder;
import org.xiph.speex.SpeexEncoder;

import java.io.StreamCorruptedException;

public class SpeexCodec extends VoiceCodec {
	private SpeexDecoder decoder;
	private SpeexEncoder encoder;
	
	private int mode;
	
	public SpeexCodec(int sampleRate, int channels, int mode) {
		super(sampleRate, channels);
		
		this.mode = mode;
	}
	
	@Override
	public void init() {
		decoder = new SpeexDecoder();
		decoder.init(mode, sampleRate, channels, false);
		
		encoder = new SpeexEncoder();
		encoder.init(mode, 10, sampleRate, channels); // TODO: variable quality
	}
	
	@Override
	public byte[] decode(short clientID, byte[] data) {
		try {
			if (data == null) {
				decoder.processData(true);
			} else {
				decoder.processData(data, 0, data.length);
			}
			
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
		encoder.processData(data, 0, data.length);
		byte[] out = new byte[encoder.getProcessedDataByteSize()];
		encoder.getProcessedData(out, 0);
		return out;
	}
	
	@Override
	public void dispose() {}
}
