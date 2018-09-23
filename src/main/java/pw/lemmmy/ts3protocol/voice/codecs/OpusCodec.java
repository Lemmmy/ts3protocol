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
	
	private ByteBuffer outputByteBuffer;
	private ShortBuffer outputBuffer;
	
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
		
		opus.opus_encoder_ctl(encoder, Opus.OPUS_SET_BITRATE_REQUEST, 8192 * Short.BYTES * channels);
		
		outputByteBuffer = ByteBuffer.allocateDirect(SEGMENT_FRAMES * Short.BYTES * channels);
		outputBuffer = outputByteBuffer.asShortBuffer();
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
		outputByteBuffer.rewind();
		
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
		ByteBuffer inputBuffer = ByteBuffer.allocateDirect(data.length);
		inputBuffer.put(data);
		
		int outputLength = SEGMENT_FRAMES * Short.BYTES * channels;
		ByteBuffer outputByteBuffer = ByteBuffer.allocateDirect(outputLength);
		
		int encodedLength = opus.opus_encode(encoder, inputBuffer.asShortBuffer(), SEGMENT_FRAMES, outputByteBuffer, outputLength);
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
