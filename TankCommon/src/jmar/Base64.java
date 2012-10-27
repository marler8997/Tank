package jmar;

import jmar.test.Test;
import jmar.test.TestFailure;

public class Base64 {
	public static final char DECRYPT_ERROR = (char)-1;

	static char[] enc64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	static char[] dec64 = {
       // (index)       (ascii char)
		  DECRYPT_ERROR, // 0
		  DECRYPT_ERROR, // 1
		  DECRYPT_ERROR, // 2
		  DECRYPT_ERROR, // 3 
		  DECRYPT_ERROR, // 4
		  DECRYPT_ERROR, // 5
		  DECRYPT_ERROR, // 6
		  DECRYPT_ERROR, // 7
		  DECRYPT_ERROR, // 8
		  DECRYPT_ERROR, // 9
		  DECRYPT_ERROR, // 10
		  DECRYPT_ERROR, // 11
		  DECRYPT_ERROR, // 12
		  DECRYPT_ERROR, // 13
		  DECRYPT_ERROR, // 14
		  DECRYPT_ERROR, // 15
		  DECRYPT_ERROR, // 16
		  DECRYPT_ERROR, // 17
		  DECRYPT_ERROR, // 18
		  DECRYPT_ERROR, // 19
		  DECRYPT_ERROR, // 20
		  DECRYPT_ERROR, // 21
		  DECRYPT_ERROR, // 22
		  DECRYPT_ERROR, // 23
		  DECRYPT_ERROR, // 24
		  DECRYPT_ERROR, // 25
		  DECRYPT_ERROR, // 26
		  DECRYPT_ERROR, // 27
		  DECRYPT_ERROR, // 28
		  DECRYPT_ERROR, // 29
		  DECRYPT_ERROR, // 30
		  DECRYPT_ERROR, // 31
		  DECRYPT_ERROR, // 32
		  DECRYPT_ERROR, // 33
		  DECRYPT_ERROR, // 34
		  DECRYPT_ERROR, // 35
		  DECRYPT_ERROR, // 36
		  DECRYPT_ERROR, // 37
		  DECRYPT_ERROR, // 38
		  DECRYPT_ERROR, // 39
		  DECRYPT_ERROR, // 40
		  DECRYPT_ERROR, // 41
		  DECRYPT_ERROR, // 42
		  62,            // 43 '+'
		  DECRYPT_ERROR, // 44
		  DECRYPT_ERROR, // 45
		  DECRYPT_ERROR, // 46
		  63,            // 47 '/'
		  52,            // 48 '0'
		  53,            // 49 '1'
		  54,            // 50 '2'
		  55,            // 51 '3'
		  56,            // 52 '4'
		  57,            // 53 '5'
		  58,            // 54 '6'
		  59,            // 55 '7'
		  60,            // 56 '8'
		  61,            // 57 '9'
		  DECRYPT_ERROR, // 58
		  DECRYPT_ERROR, // 59
		  DECRYPT_ERROR, // 60
		  DECRYPT_ERROR, // 61
		  DECRYPT_ERROR, // 62
		  DECRYPT_ERROR, // 63
		  DECRYPT_ERROR, // 64
		  0,             // 65 'A'
		  1,             // 66 'B'
		  2,             // 67 'C'
		  3,             // 68 'D'
		  4,             // 69 'E'
		  5,             // 70 'F'
		  6,             // 71 'G'
		  7,             // 72 'H'
		  8,             // 73 'I'
		  9,             // 74 'J'
		  10,            // 75 'K'
		  11,            // 76 'L'
		  12,            // 77 'N'
		  13,            // 78 'M'
		  14,            // 79 'O'
		  15,            // 80 'P'
		  16,            // 81 'Q'
		  17,            // 82 'R'
		  18,            // 83 'S'
		  19,            // 84 'T'
		  20,            // 85 'U'
		  21,            // 86 'V'
		  22,            // 87 'W'
		  23,            // 88 'X'
		  24,            // 89 'Y'
		  25,            // 90 'Z'
		  DECRYPT_ERROR, // 91
		  DECRYPT_ERROR, // 92
		  DECRYPT_ERROR, // 93
		  DECRYPT_ERROR, // 94
		  DECRYPT_ERROR, // 95
		  DECRYPT_ERROR, // 96
		  26,            // 97  'a'
		  27,            // 98  'b'
		  28,            // 99  'c'
		  29,            // 100 'd'
		  30,            // 101 'e'
		  31,            // 102 'f'
		  32,            // 103 'g'
		  33,            // 104 'h'
		  34,            // 105 'i'
		  35,            // 106 'j'
		  36,            // 107 'k'
		  37,            // 108 'l'
		  38,            // 109 'm'
		  39,            // 110 'n'
		  40,            // 111 'o'
		  41,            // 112 'p'
		  42,            // 113 'q'
		  43,            // 114 'r'
		  44,            // 115 's'
		  45,            // 116 't'
		  46,            // 117 'u'
		  47,            // 118 'v'
		  48,            // 119 'w'
		  49,            // 120 'x'
		  50,            // 121 'y'
		  51,            // 122 'z'
		  DECRYPT_ERROR, // 123
		  DECRYPT_ERROR, // 124
		  DECRYPT_ERROR, // 125
		  DECRYPT_ERROR, // 126
		  DECRYPT_ERROR, // 127
		};

	public static byte[] decode(String ascii) {
		
		if(ascii == null) return null;
		int asciiLength = ascii.length();
		
		// get non padded length
		int nonPaddedLength;
		for(nonPaddedLength = asciiLength; nonPaddedLength > 0 && ascii.charAt(nonPaddedLength - 1) == '='; nonPaddedLength--);	
		
		
		byte[] bytes = new byte[base64NonPaddedLengthToBinaryLength(nonPaddedLength)];
		int actualDecodeLength = decode(ascii, bytes, 0);
		if(actualDecodeLength != bytes.length) throw new IllegalStateException(String.format("Code Bug: Expected decode length to be %d but it was %d", bytes.length, actualDecodeLength));
		return bytes;
	}
	
	// returns size of decoded data in bytes
	public static int decode(String ascii, byte[] binary, int offset) {
		int asciiIndex = 0;
		int asciiLength = ascii.length();
		
		int originalOffset = offset;
		
		while(asciiIndex < asciiLength && (ascii.charAt(asciiIndex) != '=')) {
			char temp1 = dec64[ascii.charAt(asciiIndex + 1)];
			
			binary[offset++] = (byte) ((dec64[ascii.charAt(asciiIndex)] << 2) | (temp1 >> 4));
			asciiIndex += 2;
			if(asciiIndex >= asciiLength || ascii.charAt(asciiIndex) == '=') break;
			
			char temp2 = dec64[ascii.charAt(asciiIndex)];

			binary[offset++] = (byte)((temp1 << 4) | (temp2 >> 2));
			asciiIndex++;
			if(asciiIndex >= asciiLength || ascii.charAt(asciiIndex) == '=') break;
			

			binary[offset++] = (byte)(temp2 << 6 | dec64[ascii.charAt(asciiIndex)]);
			asciiIndex++;
		}
		return offset - originalOffset;
	}

	
	public static String encode(byte[] binary, int offset, int length) {
		char [] encodedChars = new char[binaryLengthToBase64Length(length)];
		int encodeLength = encode(binary, offset, length, encodedChars);
		if(encodeLength != encodedChars.length) throw new IllegalStateException(String.format("Code Bug in Base64 Decode(encodeLength=%d, encodedChars.length=%d)", encodeLength, encodedChars.length));
		return new String(encodedChars);
	}
	public static int encode(byte[] binary, int offset, int length, char[] ascii) {
		int asciiIndex = 0;
		for(; length > 2; length -= 3) {
		    ascii[asciiIndex++] = enc64[ (0xFF & binary[offset]) >> 2 ];
		
		    ascii[asciiIndex++] = enc64[ 0x3F & (((0xFF & binary[offset]) << 4) | ((0xFF & binary[offset + 1]) >> 4)) ];
		    offset++;
		
		    ascii[asciiIndex++] = enc64[ 0x3F & ((0xFF & binary[offset]) << 2 | ((0xFF & binary[offset + 1]) >> 6))   ];
		    offset++;
		
		    ascii[asciiIndex++] = enc64[ 0x3F & (0xFF & binary[offset]) ];
		    offset++;
		}		
		if(length == 2) {
			ascii[asciiIndex++] = enc64[ (0xFF & binary[offset]) >> 2 ];
		
			ascii[asciiIndex++] = enc64[ 0x3F & (((0xFF & binary[offset]) << 4) | ((0xFF & binary[offset + 1]) >> 4)) ];
			offset++;
		
		    ascii[asciiIndex++] = enc64[ 0x3F & ((0xFF & binary[offset]) << 2 | (0 >> 6))   ];
		    
		    ascii[asciiIndex++] = '=';
		} else if(length == 1) {
			ascii[asciiIndex++] = enc64[ (0xFF & binary[offset]) >> 2 ];
		
			ascii[asciiIndex++] = enc64[ 0x3F & (((0xFF & binary[offset]) << 4) | (0 >> 4)) ];
			
		    ascii[asciiIndex++] = '=';
		    ascii[asciiIndex++] = '=';
		}		
		return asciiIndex;
	}
	
	public static int binaryLengthToBase64Length(int binaryLength) {
		int mod3 = binaryLength % 3;
		int temp = (mod3 != 0) ? binaryLength + 3 - mod3 : binaryLength;
		return (temp / 3) * 4;
	}
	
	public static int base64NonPaddedLengthToBinaryLength(int base64NonPaddedLength) {
		return (base64NonPaddedLength * 3) / 4;
	}
	
	public static void main(String[] args) throws TestFailure {
		/*
		for(int i = 0; i < 10; i++) {
			System.out.println(i + ": " + binaryLengthToBase64Length(i));
		}
		*/
		
		byte [][] binaryTests = new byte[][] {
			new byte[0],
			new byte[] {0},
			new byte[] {1,2},
			new byte[] {1,2,3},
			new byte[] {1,2,3,4},
			"Hello how are you?".getBytes(),
			"Another tes".getBytes()
		};
		
		for(int i = 0; i < binaryTests.length; i++) {
			byte[] test = binaryTests[i];
			
			String encoded = encode(test, 0, test.length);		
			System.out.println(String.format("Encoded = '%s'", encoded));
			
			byte[] decoded = decode(encoded);
			Test.assertEqual(decoded.length, test.length);
			for(int j = 0; j < test.length; j++) {
				Test.assertEqual(decoded[j], test[j]);
			}			
		}
		
		
	}
	
}
