package jmar.games.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class InputStreamChunker {
	
	public final InputStream inputStream;
	
	private int currentChunkBytesLeft;
	public byte[] bytes;
	private int currentChunkOffset;
	
	public InputStreamChunker(InputStream inputStream) {
		this.inputStream = inputStream;
		this.currentChunkBytesLeft = 0; // 0 means we have not read the chunk size for the current chunk
	}
	
	public int readChunkFullBlocking() throws IOException {		
		if(currentChunkBytesLeft != 0) throw new IllegalStateException("currentChunkBytesLeft must be 0");
		
		// Get Chunk Size
		int chunkBytesLeft;
		
		int byteAsInt = inputStream.read();
		if(byteAsInt < 0) throw new IOException("Connection closed");
		chunkBytesLeft  = (0xFF00 & (byteAsInt << 8));
		
		byteAsInt = inputStream.read();
		if(byteAsInt < 0) throw new IOException("Connection closed");		
		chunkBytesLeft |= (0x00FF & byteAsInt);
		
		if(chunkBytesLeft < 0 || chunkBytesLeft > 0xFFFF) {
			throw new IOException(String.format("Encoded Chunk size must be within 0 and 65535 but was %d", currentChunkBytesLeft));
		}
		chunkBytesLeft++; // The encoded length is the actual chunk size minus 1
		
		if(bytes == null || bytes.length < chunkBytesLeft) {
			bytes = new byte[chunkBytesLeft];
		}
		int chunkOffset = 0;
		
		// Read bytes
        int lastBytesRead;
        do
        {
            lastBytesRead = inputStream.read(bytes, chunkOffset, chunkBytesLeft);
            chunkBytesLeft -= lastBytesRead;
            chunkOffset += lastBytesRead;
            if (chunkBytesLeft <= 0) return chunkOffset;
        } while (lastBytesRead > 0);

        throw new IOException("Connection closed");
	}


	// Returns 0 if chunk was not read and chunk size if full chunk was read
	public int readOne() throws IOException {
		if(currentChunkBytesLeft <= 0) {
			currentChunkBytesLeft  = (0xFF00 & (inputStream.read() << 8));
			currentChunkBytesLeft |= (0x00FF & (inputStream.read()     ));

			if(currentChunkBytesLeft < 0 || currentChunkBytesLeft > 0xFFFF) {
				throw new IOException(String.format("Chunk size must be within 0 and 65535 but was %d", currentChunkBytesLeft));
			}
			currentChunkBytesLeft++; // The encoded length is the actual chunk size minus 1
			
			
			if(bytes == null || bytes.length < currentChunkBytesLeft) {
				bytes = new byte[currentChunkBytesLeft];
			}
			this.currentChunkOffset = 0;
			return 0;
		}
		
		while(true) {			
			int bytesRead = inputStream.read(bytes, currentChunkOffset, currentChunkBytesLeft);
			if(bytesRead <= 0) throw new IOException();
			
			currentChunkBytesLeft -= bytesRead;
			currentChunkOffset += bytesRead;
			return currentChunkBytesLeft;
		}
	}

	// Returns 0 if chunk was not read and chunk size if full chunk was read
	public int readChunkWhileAvailable() throws IOException {
		int available;
		
		if(currentChunkBytesLeft <= 0) {
			available = inputStream.available();
			if(available < 2) return 0; // wait for more data			

			currentChunkBytesLeft  = (0xFF00 & (inputStream.read() << 8));
			currentChunkBytesLeft |= (0x00FF & (inputStream.read()     ));

			if(currentChunkBytesLeft < 0 || currentChunkBytesLeft > 0xFFFF) {
				throw new IOException(String.format("Chunk size must be within 0 and 65535 but was %d", currentChunkBytesLeft));
			}
			currentChunkBytesLeft++; // The encoded length is the actual chunk size minus 1
			
			if(bytes == null || bytes.length < currentChunkBytesLeft) {
				bytes = new byte[currentChunkBytesLeft];
			}
			this.currentChunkOffset = 0;
		}
		
		while(true) {
			available = inputStream.available();
			if(available <= 0) return 0;
			
			int readLength = (available <= currentChunkBytesLeft) ? available : currentChunkBytesLeft;
			
			int bytesRead = inputStream.read(bytes, currentChunkOffset, readLength);
			if(bytesRead <= 0) throw new IOException();
			
			currentChunkBytesLeft -= bytesRead;
			currentChunkOffset += bytesRead;
			if(currentChunkBytesLeft <= 0) {
				System.out.print(String.format("[Chunker] Received %d bytes:", currentChunkOffset));
				for(int i = 0; i < currentChunkOffset; i++) {
					System.out.print(' ');
					System.out.print(bytes[i]);
				}
				System.out.println();
				
				return currentChunkOffset;
			}
		}
	}
	
}
