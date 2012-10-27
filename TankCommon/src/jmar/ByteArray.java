package jmar;

import javax.xml.bind.DatatypeConverter;

public class ByteArray {
	public static final String[] asciiMap = new String[] {
		"\\0", //
		"\\x01", //
		"\\x02", //
		"\\x03", //
		"\\x04", //
		"\\x05", //
		"\\x06", //
		"\\x07", //
		"\\x08", //
		"\\t", //
		"\\n", // 
		"\\x0B", // 
		"\\x0C", // 
		"\\x0D", // 
		"\\x0E", // 
		"\\x0F", // 
		"\\x10", //
		"\\x11", //
		"\\x12", //
		"\\r", //
		"\\f", //
		"\\x15", //
		"\\x16", //
		"\\x17", //
		"\\x18", //
		"\\x19", //
		"\\x1A", //
		"\\x1B", //
		"\\x1C", //
		"\\x1D", //
		"\\x1E", //
		"\\x1F", //
		" ", // 0x20
		"!", // 0x21
		"\"", // 0x22
		"#", // 0x23
		"$", // 0x24
		"%", // 0x25
		"&", // 0x26
		"'", // 0x27
		"(", // 0x28
		")", // 0x29
		"*", // 0x2A
		"+", // 0x2B
		",", // 0x2C
		"-", // 0x2D
		".", // 0x2E
		"/", // 0x2F
		"0", // 0x30
		"1", // 0x31
		"2", // 0x32
		"3", // 0x33
		"4", // 0x34
		"5", // 0x35
		"6", // 0x36
		"7", // 0x37
		"8", // 0x38
		"9", // 0x39
		":", // 0x3A
		";", // 0x3B
		"<", // 0x3C
		"=", // 0x3D
		">", // 0x3E
		"?", // 0x3F
		"@", // 0x40
		"A", // 0x41
		"B", // 0x42
		"C", // 0x43
		"D", // 0x44
		"E", // 0x45
		"F", // 0x46
		"G", // 0x47
		"H", // 0x48
		"I", // 0x49
		"J", // 0x4A
		"K", // 0x4B
		"L", // 0x4C
		"M", // 0x4D
		"N", // 0x4E
		"O", // 0x4F
		"P", // 0x4
		"", // 0x4
		"", // 0x4
		"", // 0x4
		""
		
		
	};
	
	
	
	
	
	
	/*
	public static String toHexString(byte[] array) {
		if(array == null) return "";
		char[] chars = new char[array.length * 2];
		int charIndex = 0;
		for(int i = 0; i < array.length; i++) {
			byte b = array[i];
			byte first = (byte)(0xF & (b >> 4));
			if(first <= 9) {
				chars[charIndex++] = (char)(first + '0');
			} else {
				chars[charIndex++] = (char)(first + 'A' - 10);
			}
			b &= 0xF;
			if(b <= 9) {
				chars[charIndex++] = (char)(b + '0');
			} else {
				chars[charIndex++] = (char)(b + 'A' - 10);
			}			
		}
		return new String(chars);
	}
	*/
	
	public static String toEscapString(byte[] array, int offset, int length) {
		if(array == null) return null;
		StringBuilder builder = new StringBuilder();		
		
		int limit = offset + length;
		while(true) {
			if(offset >= limit) return builder.toString();
			
			byte b = array[offset++];
			if(b >= 0x20 && b <= 0x7E) {
				builder.append((char)b);
				continue;
			} else {
				// non printable
				builder.append("\\x");
				byte first = (byte)(0xF & (b >> 4));
				if(first <= 9) {
					builder.append((char)(first + '0'));
				} else {
					builder.append((char)(first + 'A' - 10));
				}
				b &= 0xF;
				if(b <= 9) {
					builder.append((char)(b + '0'));
				} else {
					builder.append((char)(b + 'A' - 10));
				}				
			}
			
			
			
		}
	}
	
	
	public static void main(String[] args) {
		byte[] zeroThrough20 = new byte[21];
		for(int i = 0; i < zeroThrough20.length; i++) {
			zeroThrough20[i] = (byte)i;
		}
		
		
		
		System.out.println(toEscapString(zeroThrough20, 0, zeroThrough20.length));
		
		
		byte[] test = new byte[] {
				0, 1, 'a', '\n', '\r', 'b', '~'
		};
		System.out.println(toEscapString(test, 0, test.length));
		
	}
	
	public static byte[] subArray(byte[] array, int offset, int length) {
		byte[] subArray = new byte[length];
		for(int i = 0; i < subArray.length; i++) {
			subArray[i] = array[offset++];
		}
		return subArray;
	}
	
	public static String toHexString(byte[] array, int offset, int length) {
		return toHexString(subArray(array, offset, length));
	}
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}
	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
}
