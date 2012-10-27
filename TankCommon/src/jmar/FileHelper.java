package jmar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class FileHelper {
	
	public static String readFileToString(File file) throws IOException {
		return new String(readFile(file));
	}
	
	public static byte[] readFile(File file) throws IOException {		
		FileInputStream stream = new FileInputStream(file);
		try {
			byte[] bytes = new byte[(int) file.length()];
			StreamHelper.readFullLength(stream, bytes, 0, bytes.length);
			return bytes;
		} finally {
			try {stream.close();} catch(IOException e) {e.printStackTrace();}
		}
	}
	
	public static void writeFile(File file, String contents) throws IOException {
		FileWriter fileWriter = new FileWriter(file, false);
		try {
			fileWriter.write(contents);
		} finally {
			try{fileWriter.close();} catch(IOException e) { }
		}
	}
}
