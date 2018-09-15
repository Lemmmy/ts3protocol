package pw.lemmmy.ts3protocol.voice.codecs;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

public class OpusCodec extends VoiceCodec {
	private static final int SEGMENT_FRAMES = 960;
	
	private Opus opus;
	private PointerByReference encoder;
	private Map<Short, PointerByReference> decoders = new HashMap<>();
	private int mode;
	
	public OpusCodec(int sampleRate, int channels, int mode) {
		super(sampleRate, channels);
		
		this.mode = mode;
	}
	
	@Override
	public void init() {
		try {
			OpusLibrary.loadFromJar();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		opus = Opus.INSTANCE;
		
		IntBuffer error = IntBuffer.allocate(1);
		encoder = opus.opus_encoder_create(sampleRate, channels, mode, error);
		if (error.get() < 0) {
			throw new RuntimeException("Failed to create opus encoder: " + opus.opus_strerror(error.get()));
		}
	}
	
	private PointerByReference getOrCreateDecoder(short clientID) {
		if (decoders.containsKey(clientID)) return decoders.get(clientID);
		
		IntBuffer error = IntBuffer.allocate(1);
		PointerByReference decoder = opus.opus_decoder_create(sampleRate, channels, error);
		if (error.get() < 0) {
			throw new RuntimeException("Failed to create opus decoder: " + opus.opus_strerror(error.get()));
		}
		decoders.put(clientID, decoder);
		return decoder;
	}
	
	@Override
	public byte[] decode(short clientID, byte[] data) {
		ByteBuffer outputByteBuffer = ByteBuffer.allocateDirect(SEGMENT_FRAMES * Short.BYTES * channels);
		ShortBuffer outputBuffer = outputByteBuffer.asShortBuffer();
		
		int length = opus.opus_decode(
			getOrCreateDecoder(clientID),
			data, data != null ? data.length : 0,
			outputBuffer, SEGMENT_FRAMES, 0
		);
		if (length < 0) return null;
		
		byte[] out = new byte[length * Short.BYTES * channels];
		outputByteBuffer.get(out);
		return out;
	}
	
	@Override
	public byte[] encode(byte[] data) {
		ShortBuffer inputBuffer = ByteBuffer.wrap(data).asShortBuffer();
		
		int outputLength = SEGMENT_FRAMES * Short.BYTES * channels;
		ByteBuffer outputByteBuffer = ByteBuffer.allocateDirect(outputLength);
		
		int encodedLength = opus.opus_encode(encoder, inputBuffer, data.length / channels, outputByteBuffer, SEGMENT_FRAMES * channels);
		if (encodedLength < 0) return null;
		
		byte[] out = new byte[encodedLength];
		outputByteBuffer.get(out);
		return out;
	}
	
	@Override
	public void dispose() {
		decoders.values().forEach(opus::opus_decoder_destroy);
		if (encoder != null) opus.opus_encoder_destroy(encoder);
	}
}
