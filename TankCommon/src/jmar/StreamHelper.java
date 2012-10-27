package jmar;

import java.io.IOException;
import java.io.InputStream;


public class StreamHelper {
	public static void readFullLength(InputStream stream, byte[] buffer, int offset, int length) throws IOException {
        int lastBytesRead;

        do
        {
            lastBytesRead = stream.read(buffer, offset, length);
            length -= lastBytesRead;
            if (length <= 0) return;
            offset += lastBytesRead;
        } while (lastBytesRead > 0);

        throw new IOException(String.format("IOException but still needed %d bytes", length));		
	}
}
