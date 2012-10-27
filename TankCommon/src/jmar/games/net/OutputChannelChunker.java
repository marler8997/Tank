package jmar.games.net;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public class OutputChannelChunker {	
	public final WritableByteChannel outputChannel;
	
	public OutputChannelChunker(WritableByteChannel outputChannel) {
		this.outputChannel = outputChannel;
	}

	public void sendChunk(ChunkByteBuffer chunk) throws IOException {
		System.out.print(String.format("[Chunker] Sending %d byte chunk:", chunk.chunkBuffer.remaining() - 2));
		for(int i = 2; i < chunk.chunkBuffer.remaining(); i++) {
			System.out.print(' ');
			System.out.print(chunk.chunkBuffer.get(i));
		}
		System.out.println();
		
		outputChannel.write(chunk.chunkBuffer);
	}

	public void sendChunkAndResetBuffer(ChunkByteBuffer chunk) throws IOException {
		this.sendChunk(chunk);
		chunk.chunkBuffer.position(0);
		chunk.chunkBuffer.limit(chunk.finishedChunkSize + 2);
	}
	

}
