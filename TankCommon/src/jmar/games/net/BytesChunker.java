package jmar.games.net;

import jmar.test.Test;
import jmar.test.TestFailure;

//
// This class must be tested very very highly
//
public class BytesChunker {
	public byte[] bytes;
	private int bytesOffset;

	private int currentChunkSize;
	private int bytesReturnedInLastChunk;
	
	public BytesChunker(int initialCapacity) {
		this.bytes                = new byte[initialCapacity];
		this.bytesOffset              = 0;
		this.currentChunkSize         = 0;
		this.bytesReturnedInLastChunk = 0;	
		
		//
		// When the currentChunkSize is 0 (meaning we have not yet read the current chunk size)
		// then the bytesOffset will always be 0 or 1
	}
	
	public void reset() {
		this.bytesOffset              = 0;
		this.currentChunkSize         = 0;
		this.bytesReturnedInLastChunk = 0;			
	}
	
	private void mustHoldAtLeast(int size) {
		if(size >= bytes.length) {
			byte[] newBiggerBytes = new byte[size + 32];
			for(int i = 0; i < bytesOffset; i++) {
				newBiggerBytes[i] = bytes[i];
			}
			bytes = newBiggerBytes;
		}		
	}
	
	public void addBytes(byte[] newBytes, int offset, int size) {
		if(size <= 0) return;
		
		if(currentChunkSize <= 0) {
			if(bytesOffset < 1) {
				if(size <= 1) {
					bytes[bytesOffset++] = newBytes[0];
					return;
				}
				
				this.currentChunkSize = ((0xFF00 & (newBytes[offset    ] << 8)) |
				 		 				 (0x00FF & (newBytes[offset + 1]     )) )
				 		 				 			+ 1;
				offset += 2;
				size -= 2;
			} else {
				// bytesOffset == 1
				this.currentChunkSize = ((0xFF00 & (bytes   [     0] << 8)) |
										 (0x00FF & (newBytes[offset]     )) )
										 			+ 1;
				offset ++;
				size--;
				bytesOffset = 0;
			}
		}
		
		//
		// At this point, bytesOffset will represent how many of the current chunk bytes are in the buffer
		//
		
		// Make the bytes bigger if necessary
		mustHoldAtLeast(bytesOffset + size);
		
		// Put the new bytes in
		for(int i = 0; i < size; i++) {
			bytes[bytesOffset++] = newBytes[offset + i];
		}
	}
	
	public int getChunk() {
		//
		// Copy any bytes left over from the next chunk if there are any
		//
		if(bytesReturnedInLastChunk > 0) {
			int extraBytes = bytesOffset - bytesReturnedInLastChunk;
			if(extraBytes < 2) return 0; // wait until more bytes are added

			currentChunkSize = ((0xFF00 & (bytes[bytesReturnedInLastChunk    ] << 8)) |
	 				 			(0x00FF & (bytes[bytesReturnedInLastChunk + 1]     )) )
	 				 				 	+ 1;
			
			for(int i = 0; bytesReturnedInLastChunk + 2 + i < bytesOffset; i++) {
				bytes[i] = bytes[bytesReturnedInLastChunk + 2 + i];
			}
			bytesOffset -= (bytesReturnedInLastChunk + 2);
			bytesReturnedInLastChunk = 0;
		}		
		
		if(currentChunkSize <= 0) return 0;		
		
		
		if(currentChunkSize == bytesOffset) {
			int size = currentChunkSize;
			bytesOffset = 0;
			currentChunkSize = 0;
			return size;
		}		
		
		if(currentChunkSize < bytesOffset) {
			bytesReturnedInLastChunk = currentChunkSize;
			
			currentChunkSize = 0;
			return bytesReturnedInLastChunk;
		}
		
		return 0;
	}
	
	
	public static void main(String[] args) throws TestFailure {
		BytesChunker chunker = new BytesChunker(4);

		chunker.addBytes(null, 0, 0);	
		
		
		byte[] oneByteArray = new byte[1];
		byte[] fourByteArray = new byte[4];
		
		oneByteArray[0] = 0;
		chunker.addBytes(oneByteArray, 0, 1);
		Test.assertEqual(chunker.bytes[0], 0);
		Test.assertEqual(chunker.bytesOffset, 1);
		
		chunker.addBytes(oneByteArray, 0, 1);
		Test.assertEqual(chunker.bytesOffset, 0);
		Test.assertEqual(chunker.currentChunkSize, 1);
		
		
		chunker.reset();
		
		
		oneByteArray[0] = 4;
		chunker.addBytes(oneByteArray, 0, 1);

		Test.assertEqual(chunker.bytes[0], 4);
		Test.assertEqual(chunker.bytesOffset, 1);
		
		oneByteArray[0] = 8;
		chunker.addBytes(oneByteArray, 0, 1);
		

		Test.assertEqual(chunker.bytesOffset, 0);
		Test.assertEqual(chunker.currentChunkSize, 0x408 + 1);

		
		chunker.reset();
		

		oneByteArray[0] = 0;
		chunker.addBytes(oneByteArray, 0, 1);
		Test.assertEqual(chunker.bytes[0], 0);
		Test.assertEqual(chunker.bytesOffset, 1);
		
		chunker.addBytes(oneByteArray, 0, 1);
		Test.assertEqual(chunker.bytesOffset, 0);
		Test.assertEqual(chunker.currentChunkSize, 1);
		
		chunker.addBytes(oneByteArray, 0, 1);
		Test.assertEqual(chunker.bytesOffset, 1);
		Test.assertEqual(chunker.currentChunkSize, 1);
		
		chunker.addBytes(fourByteArray, 0, 4);
		Test.assertEqual(chunker.bytesOffset, 5);
		Test.assertEqual(chunker.currentChunkSize, 1);
		System.out.println("bytes.length = " + chunker.bytes.length);
		
		Test.assertEqual(1, chunker.getChunk());
		Test.assertEqual(1, chunker.getChunk());
		Test.assertEqual(0, chunker.getChunk());
		
		
		//
		// The Two Chunk Split Test
		//
		byte[] twoChunks = new byte[] {
				(byte)0, (byte)2,
					(byte)0xA1, (byte)0xFE, (byte)0x53,
				(byte)0, (byte)0,
					(byte) 0x7F
		};
		
		chunker.reset();
		for(int split = 0; split < twoChunks.length - 1; split++) {
			System.out.println(String.format("split = %d", split));
			chunker.addBytes(twoChunks, 0, split + 1);
			chunker.addBytes(twoChunks, split + 1, twoChunks.length - split - 1);

			Test.assertEqual(3, chunker.getChunk());
			Test.assertEqual(twoChunks[2], chunker.bytes[0]);
			Test.assertEqual(twoChunks[3], chunker.bytes[1]);
			Test.assertEqual(twoChunks[4], chunker.bytes[2]);
			Test.assertEqual(1, chunker.getChunk());
			Test.assertEqual(twoChunks[7], chunker.bytes[0]);
			Test.assertEqual(0, chunker.getChunk());
		}
		
		
		//
		// Random Test
		//
		byte[] randomTest = new byte[] {
				(byte)0x00, (byte) 0x09,
				(byte)0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, (byte) 0xED, (byte) 0xCB
		};
		chunker.reset();
		chunker.addBytes(randomTest, 0, randomTest.length);
		Test.assertEqual(10, chunker.getChunk());
	}
	
}