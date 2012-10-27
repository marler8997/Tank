package jmar.games.tank;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.RuntimeErrorException;

import jmar.Base64;
import jmar.FileHelper;
import jmar.MacAddress;
import jmar.ShouldntHappenException;

public class TankDecryption {
	// This string is the base64 encoded characters from the public key PEM file
	public static final String publicKeyBase64 =
			"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwu63bHEFCq37NvEdR7GHgtFnuCryL7dvIZjduGVzl4mfoxVGfg+7JA4HMHPr2Gc8sj3tPIlD+kVTSlSufYvtfTyC/LcYF5RsVpPYfXlyJJ3y7/KpH62VCl4mHX1OkjBoayjG0KSCcN/Adim1kB4oKT3cOslfPV0nGi0BBjjoPywIDAQAB";
	public static final byte[] publicKeyBytes = Base64.decode(publicKeyBase64);
	private static PublicKey cachedPublicKey = null;
	private static Cipher cachedCipher = null;
	
	private static PublicKey getPublicKey() {
		try {
			if(cachedPublicKey == null) {
				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				cachedPublicKey = kf.generatePublic(keySpec);
			}
			return cachedPublicKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new ShouldntHappenException(e);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			throw new ShouldntHappenException(e);
		}
	}
	
	private static Cipher getCipher() {
		try {
			if(cachedCipher == null) {
				cachedCipher = Cipher.getInstance("RSA");
			}
			return cachedCipher;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new ShouldntHappenException(e);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new ShouldntHappenException(e);
		}	
	}
	
	public static byte[] decrypt(byte[] data, int offset, int length) throws IllegalBlockSizeException, BadPaddingException {
		PublicKey publicKey = TankDecryption.getPublicKey();
		Cipher cipher = TankDecryption.getCipher();
		try {
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new ShouldntHappenException(e);
		}

		//System.out.println("[Debug] decrypting: " + ByteArray.toHexString(offlineKeyBytes));
		return cipher.doFinal(data, offset, length);
	}
}
