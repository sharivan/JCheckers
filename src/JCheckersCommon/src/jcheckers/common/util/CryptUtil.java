package jcheckers.common.util;

public class CryptUtil {

	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static byte[] computeKey(String s) {
		int[] k = new int[8];
		int d1 = 0;
		int d2 = 0;
		int c1 = 0;
		int c2 = 0;
		for (int i = 0; i < 8; i++)
			k[i] = 0;

		int j = 0;
		for (int i = 0; i < s.length(); i++) {
			d1 = s.charAt(i);
			k[j] += d1;
			k[j] &= 0xff;
			j++;
			if (j == 8)
				j = 0;
		}

		j = 0;
		byte[] key = new byte[16];
		for (int i = 0; i < 8; i++) {
			d1 = k[i];
			d2 = d1 >>> 4;
			c2 = HEX_DIGITS[d2];
			d1 = d1 & 0xf;
			c1 = HEX_DIGITS[d1];

			key[j] = (byte) (c2 & 0xff);
			j++;

			key[j] = (byte) (c1 & 0xff);
			j++;
		}

		return key;
	}

	public static void crypt1(byte[] buf, int off, int len) {
		// TODO implementar
	}

	public static void crypt2(byte[] buf, int off, int len) {
		// TODO implementar
	}

	public static void crypt3(byte[] buf, int off, int len) {
		synchronized (buf) {
			for (int i = off; i < off + len / 2; i++) {
				byte temp = buf[i];
				int tempOff = len - i + off - 1;
				buf[i] = buf[tempOff];
				buf[tempOff] = temp;
			}
		}
	}

	public static void crypt4(byte[] buf, int off, int len) {
		synchronized (buf) {
			for (int i = off; i < off + len; i++) {
				int value = buf[i] & 0xff;
				boolean b1 = f1(value, 1);
				boolean b2 = f1(value, 2);
				boolean b3 = f1(value, 5);
				boolean b4 = f1(value, 6);
				value = f2(value, 1, b4);
				value = f2(value, 6, b1);
				value = f2(value, 2, b3);
				value = f2(value, 5, b2);
				buf[i] = (byte) value;
			}
		}
	}

	public static void crypt5(byte[] buf, int off, int len, byte[] key) {
		synchronized (buf) {
			int j = 0;
			for (int i = off; i < off + len; i++) {
				buf[i] ^= key[j];
				j++;
				if (j == key.length)
					j = 0;
			}
		}
	}

	private static boolean f1(int x, int y) {
		return (x >> y & 1) != 0;
	}

	private static int f2(int x, int y, boolean z) {
		if (z)
			return (x | 1 << y) & 0xff;
		return x & -((1 << y) + 1) & 0xff;
	}

}
