package common.util;

public class ParserUtil {

	public static float parseFloat(String value, float def) {
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public static int parseInt(String value, int def) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public static double parseLong(String value, double def) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public static long parseLong(String value, long def) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

}
