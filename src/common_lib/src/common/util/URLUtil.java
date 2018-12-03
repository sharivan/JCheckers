package common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLUtil {

	public static String urlDecode(String value) {
		return urlDecode(value, "UTF-8");
	}

	public static String urlDecode(String value, String cs) {
		if (value == null)
			return null;

		try {
			return URLDecoder.decode(value, cs);
		} catch (UnsupportedEncodingException e) {
		}

		return null;
	}

	public static String urlEncode(String value) {
		return urlEncode(value, "UTF-8");
	}

	public static String urlEncode(String value, String cs) {
		if (value == null)
			return null;

		try {
			return URLEncoder.encode(value, cs);
		} catch (UnsupportedEncodingException e) {
		}

		return null;
	}

}
