package jmar.games.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class InputChannelChunker {
	public final ReadableByteChannel inputChannel;	
	
	int currentChunkSize;
	
	public byte[] bytes;
	
	private byte[] chunkBytes;
	private ByteBuffer chunkBuffer;
	
	public InputChannelChunker(ReadableByteChannel inputChannel) {
		this.inputChannel = inputChannel;
		
		this.currentChunkSize = 0;
		
		this.chunkBytes = new byte[256]; // standard initial size
		this.chunkBuffer = ByteBuffer.wrap(chunkBytes);
		
		this.bytes = new byte[chunkBytes.length - 2];
	}
	
	// Returns 0 if chunk was not read
	public int readChunkNonBlocking() throws IOException {

		System.out.println(String.format("[InputChannelChunker] readable socket open=%b", inputChannel.isOpen()));
		
		int bytesRead = this.inputChannel.read(chunkBuffer);
		if(bytesRead <= 0) throw new IOException("Expecting there to be data but there wasn't?");

		if(currentChunkSize == 0) {
			if(chunkBuffer.position() < 2) return 0;
			currentChunkSize  = (0xFF00 & (chunkBytes[0] << 8));
			currentChunkSize |= (0x00FF & (chunkBytes[1]     ));
		}
		currentChunkSize++;
		
		if(chunkBuffer.position() == currentChunkSize + 2) {
			System.arraycopy(this.chunkBytes, 2, this.bytes, 0, this.currentChunkSize);
			int chunkSize = currentChunkSize;
			
			this.currentChunkSize = 0;
			this.chunkBuffer.position(0);
			
			return chunkSize;
		}
		if(chunkBuffer.position() > currentChunkSize + 2) {
			throw new UnsupportedOperationException();
		}
		return 0;
	}
}
