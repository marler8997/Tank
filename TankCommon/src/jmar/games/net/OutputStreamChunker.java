package jmar.games.net;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamChunker {
	
	public final OutputStream outputStream;
	
	public OutputStreamChunker(OutputStream outputStream) {
		this.outputStream = outputStream;
	}	
	
	public void sendChunk(byte[] bytes) throws IOException {
		this.sendChunk(bytes, 0, bytes.length);
	}

	public void sendChunk(byte[] bytes, int offset, int length) throws IOException {
		// Send Chunk Size
		int lengthEncoded = length - 1;
		outputStream.write(0xFF & (lengthEncoded >> 8));
		outputStream.write(0xFF &  lengthEncoded      );
		
		// Send Chunk
		outputStream.write(bytes, offset, length);
		outputStream.flush();
	}	
}
