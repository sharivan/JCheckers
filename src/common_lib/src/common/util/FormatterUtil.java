package common.util;

public class FormatterUtil {

	private static final char[] LHEXDIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String formatDigits(int value, int digits) {
		char[] temp = new char[digits];
		int value1 = value;
		for (int i = digits - 1; i >= 0; i--) {
			temp[i] = LHEXDIGITS[value1 % 10];
			value1 /= 10;
		}
		return new String(temp);
	}

	public static String formatMoney(long money) {
		if (money >= 1000000000L)
			return "$" + (int) (money / 1000000000L) + "B";
		else if (money >= 1000000L)
			return "$" + (int) (money / 1000000L) + "M";
		else if (money >= 1000)
			return "$" + (int) (money / 1000L) + "K";
		else
			return "$" + money;
	}

}
