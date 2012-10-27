package jmar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlEncoder {
	public static String encode(String s) {
		try {
			return URLEncoder.encode(s,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ShouldntHappenException(e);
		}
	}
}
