package common.util;

import java.util.List;

public class RandomUtil {

	private static final char[] LHEXDIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static final char[] UHEXDIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static double random() {
		double result = Math.random();
		while (result == 1)
			result = Math.random();

		return result;
	}

	public static boolean randomArray(boolean[] ai) {
		return randomArray(ai, 0, ai.length);
	}

	public static boolean randomArray(boolean[] ai, int off, int len) {
		return ai[randomRange(off, off + len - 1)];
	}

	public static byte randomArray(byte[] b) {
		return randomArray(b, 0, b.length);
	}

	public static byte randomArray(byte[] b, int off, int len) {
		return b[randomRange(off, off + len - 1)];
	}

	public static char randomArray(char[] ai) {
		return randomArray(ai, 0, ai.length);
	}

	public static char randomArray(char[] ai, int off, int len) {
		return ai[randomRange(off, off + len - 1)];
	}

	public static double randomArray(double[] ai) {
		return randomArray(ai, 0, ai.length);
	}

	public static double randomArray(double[] ai, int off, int len) {
		return ai[randomRange(off, off + len - 1)];
	}

	public static float randomArray(float[] ai) {
		return randomArray(ai, 0, ai.length);
	}

	public static float randomArray(float[] ai, int off, int len) {
		return ai[randomRange(off, off + len - 1)];
	}

	public static int randomArray(int[] ai) {
		return randomArray(ai, 0, ai.length);
	}

	public static int randomArray(int[] ai, int off, int len) {
		return ai[randomRange(off, off + len - 1)];
	}

	public static long randomArray(long[] ai) {
		return randomArray(ai, 0, ai.length);
	}

	public static long randomArray(long[] ai, int off, int len) {
		return ai[randomRange(off, off + len - 1)];
	}

	public static short randomArray(short[] ai) {
		return randomArray(ai, 0, ai.length);
	}

	public static short randomArray(short[] ai, int off, int len) {
		return ai[randomRange(off, off + len - 1)];
	}

	public static <T> T randomArray(T[] ai) {
		return randomArray(ai, 0, ai.length);
	}

	public static <T> T randomArray(T[] ai, int off, int len) {
		return ai[randomRange(off, off + len - 1)];
	}

	public static char randomASCIIChar() {
		return (char) randomRange(0, 255);
	}

	public static char randomASCIIChar(char low, char high) {
		if (low > 255 || high > 255)
			throw new IndexOutOfBoundsException();

		return (char) randomRange(low, high);
	}

	public static String randomASCIIString(int size) {
		return randomASCIIString(size, size);
	}

	public static String randomASCIIString(int minSize, int maxSize) {
		int size = randomRange(minSize, maxSize);
		String result = "";
		for (int i = 0; i < size; i++)
			result += randomASCIIChar();

		return result;
	}

	public static String randomASCIIString(int minSize, int maxSize, char minChar, char maxChar) {
		int size = randomRange(minSize, maxSize);
		String result = "";
		for (int i = 0; i < size; i++)
			result += randomASCIIChar(minChar, maxChar);

		return result;
	}

	public static boolean randomBoolean() {
		return randomArray(new boolean[] { false, true });
	}

	public static char randomChar() {
		return (char) randomRange(0, 65536);
	}

	public static char randomChar(char low, char high) {
		return (char) randomRange(low, high);
	}

	public static char randomDigit() {
		return randomArray(LHEXDIGITS, 0, 9);
	}

	public static char randomHexDigit() {
		return randomHexDigit(false);
	}

	public static char randomHexDigit(boolean upcase) {
		if (upcase)
			return randomArray(UHEXDIGITS);

		return randomArray(LHEXDIGITS);
	}

	public static char randomHexDigit(int low, int high) {
		return randomHexDigit(low, high, false);
	}

	public static char randomHexDigit(int low, int high, boolean upcase) {
		if (upcase)
			return randomArray(UHEXDIGITS, low, high);

		return randomArray(LHEXDIGITS, low, high);
	}

	public static String randomHexString(int size) {
		return randomHexString(size, size);
	}

	public static String randomHexString(int minSize, int maxSize) {
		int size = randomRange(minSize, maxSize);
		String result = "";
		for (int i = 0; i < size; i++)
			result += randomHexDigit();

		return result;
	}

	public static String randomIP() {
		return randomRange(1, 254) + "." + randomRange(0, 254) + "." + randomRange(0, 254) + "." + randomRange(1, 254);
	}

	public static <T> T randomList(List<T> list) {
		if (list.size() == 0)
			return null;

		int index = randomRange(0, list.size() - 1);

		return list.get(index);
	}

	public static <T> T randomListAndRemove(List<T> list) {
		if (list.size() == 0)
			return null;

		int index = randomRange(0, list.size() - 1);
		T result = list.remove(index);

		return result;
	}

	public static String randomName(int minSize, int maxSize) {
		return randomASCIIString(minSize, maxSize, '0', 'z');
	}

	public static int randomRange(int lowRange, int highRange) {
		int delta = highRange - lowRange + 1;

		return (int) (random() * delta + lowRange);
	}

}
