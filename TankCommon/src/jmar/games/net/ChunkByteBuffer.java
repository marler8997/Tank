package jmar.games.net;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class ChunkByteBuffer {
	private final byte[] chunkBytes;
	public ByteBuffer chunkBuffer;
	public int finishedChunkSize;
	
	// The chunk size must be between 1 and 0x10000 inclusive
	// The bytes in the chunk packet representing the chunk size is the chunk size minus one, therefore it's range is 0 to 0xFFFF inclusive
	
	public ChunkByteBuffer(int chunkDataCapacity) {
		this.chunkBytes = new byte[chunkDataCapacity + 2];
		this.chunkBuffer = ByteBuffer.wrap(chunkBytes);
	}
	
	public void startChunk() {
		this.chunkBuffer.position(2); // Leave room for the chunk size
		this.chunkBuffer.limit(this.chunkBuffer.capacity());
	}
	
	public void endChunk() {
		finishedChunkSize = chunkBuffer.position() - 2;
		if(finishedChunkSize <= 0) throw new IllegalStateException(String.format("A Chunk cannot be %d bytes long", finishedChunkSize));
		if(finishedChunkSize > 0x10000) throw new IllegalStateException(String.format("Max chunk size is %d but your is %d", 0x10000, finishedChunkSize));
		
		int finishedChunkSizeMinusOne = finishedChunkSize - 1; // subtract 1 to store the chunk
		this.chunkBytes[0] = (byte)(finishedChunkSizeMinusOne >> 8);
		this.chunkBytes[1] = (byte) finishedChunkSizeMinusOne;
		chunkBuffer.limit(chunkBuffer.position());		
		chunkBuffer.position(0);
	}
	
	/*
	public void startFixedChunk(int chunkSize) {
		if(chunkSize <= 0 || chunkSize > 0xFFFF) {
			throw new InvalidParameterException(String.format("Chunk size must be within 0 and 65535 but was %d", chunkSize));
		}
		if(chunkSize > this.chunkBuffer.capacity() - 2) {
			throw new InvalidParameterException(String.format("Your chunk size %d is exceeding your initial capacity minus 2 (%d)", chunkSize, chunkBuffer.capacity() - 2));			
		}
		this.chunkSize = chunkSize;
		
		this.chunkBuffer.position(0);
		this.chunkBuffer.put((byte)(chunkSize >> 8));
		this.chunkBuffer.put((byte)(chunkSize     ));
		this.chunkBuffer.limit(chunkSize + 2);
	}
	
	public void endFixedChunk() {
		if(chunkBuffer.position() != chunkSize + 2) throw new IllegalStateException(String.format("Your chunk size is %d bytes but you only put in %d bytes", chunkSize, chunkBuffer.position() - 2));
		this.chunkBuffer.position(0);
	}
	*/
}
