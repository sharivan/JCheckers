package common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtil {

	public static byte[] md5(byte[] buf) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(buf);
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String md5(String password) {
		return HexUtil.toString(md5(password.getBytes()));
	}

	public static byte[] sha1(byte[] buf) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			sha1.reset();
			sha1.update(buf);
			return sha1.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String sha1(String password) {
		return HexUtil.toString(sha1(password.getBytes()));
	}

}
