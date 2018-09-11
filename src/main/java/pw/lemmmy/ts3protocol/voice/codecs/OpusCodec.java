package pw.lemmmy.ts3protocol.voice.codecs;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class OpusCodec extends VoiceCodec {
	private static final int OPUS_FRAME_SIZE = 4096;
	private static final int SEGMENT_FRAMES = 960;
	
	private Opus opus;
	private PointerByReference encoder, decoder;
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
		
		decoder = opus.opus_decoder_create(sampleRate, channels, error);
		if (error.get() < 0) {
			throw new RuntimeException("Failed to create opus decoder: " + opus.opus_strerror(error.get()));
		}
		error.rewind();
		
		encoder = opus.opus_encoder_create(sampleRate, channels, mode, error);
		if (error.get() < 0) {
			throw new RuntimeException("Failed to create opus encoder: " + opus.opus_strerror(error.get()));
		}
		error.rewind();
	}
	
	@Override
	public byte[] decode(byte[] data) {
		int outputLength = OPUS_FRAME_SIZE * channels;
		ByteBuffer outputByteBuffer = ByteBuffer.allocateDirect(outputLength * Short.SIZE);
		ShortBuffer outputBuffer = outputByteBuffer.asShortBuffer();
		
		int length = opus.opus_decode(
			decoder,
			data, data.length,
			outputBuffer, OPUS_FRAME_SIZE, 0
		);
		if (length < 0) return null;
		
		byte[] out = new byte[length * 2 * channels];
		outputByteBuffer.get(out);
		return out;
	}
	
	@Override
	public byte[] encode(byte[] data) {
		ShortBuffer inputBuffer = ByteBuffer.wrap(data).asShortBuffer();
		
		int outputLength = OPUS_FRAME_SIZE * channels;
		ByteBuffer outputByteBuffer = ByteBuffer.allocateDirect(outputLength);
		
		int encodedLength = opus.opus_encode(encoder, inputBuffer, data.length / channels, outputByteBuffer, SEGMENT_FRAMES * channels);
		if (encodedLength < 0) return null;
		
		byte[] out = new byte[encodedLength];
		outputByteBuffer.get(out);
		return out;
	}
}
