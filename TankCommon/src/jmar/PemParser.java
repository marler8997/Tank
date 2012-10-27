package jmar;

import java.io.BufferedReader;
import java.io.IOException;

public class PemParser {
	public static byte[] ParsePem(BufferedReader reader) throws IOException {
		byte[] keyBytes = new byte[512];
		int keyOffset = 0;
		
		String line;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("-")) continue;
			keyOffset += Base64.decode(line.trim(), keyBytes, keyOffset); 
		}
		
		byte[] packedBytes = new byte[keyOffset];
		System.arraycopy(keyBytes, 0, packedBytes, 0, packedBytes.length);
		
		return packedBytes;
	}

}