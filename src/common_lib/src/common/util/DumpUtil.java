package common.util;

import java.io.BufferedReader;
import java.io.IOException;

public class DumpUtil {

	@SuppressWarnings("null")
	private static byte[] concat(byte[] buf1, byte[] buf2, int len) {
		int l1 = buf1 != null ? buf1.length : 0;
		int l2 = buf2.length <= len ? buf2.length : len;
		byte[] result = new byte[l1 + l2];
		int j = 0;
		for (int i = 0; i < l1; i++)
			result[j++] = buf1[i];
		for (int i = 0; i < l2; i++)
			result[j++] = buf2[i];

		return result;
	}

	private static int parseHexByte(String line, int[] i) throws InvalidHexDigit {
		int result = HexUtil.fromDigit(line.charAt(i[0]));
		i[0]++;
		result <<= 4;
		result |= HexUtil.fromDigit(line.charAt(i[0]));
		i[0]++;

		return result;
	}

	private static int parseLine(String line, byte[] out, int off, int len) throws InvalidLine, InvalidHexDigit {
		if (off + len > out.length)
			len = out.length - off;

		int i[] = new int[1];
		i[0] = 0;
		skipBlanks(line, i);
		parseLineNumber(line, i);
		skipBlanks(line, i);
		int pos = off;
		for (int i1 = 0; i1 < 16 && pos < off + len; i1++) {
			out[pos++] = (byte) parseHexByte(line, i);

			if (line.charAt(i[0]) != ' ')
				throw new InvalidLine();

			i[0]++;
			if (line.charAt(i[0]) == ' ')
				break;
		}

		return pos - off;
	}

	private static int parseLineNumber(String line, int i[]) throws InvalidHexDigit {
		int result = parseHexByte(line, i);
		result <<= 8;
		result |= parseHexByte(line, i);
		return result;
	}

	public static byte[] readDump(BufferedReader br) throws InvalidLine, InvalidHexDigit, IOException {
		byte[] buf = new byte[512];
		byte[] result = null;

		int count = 0;
		while (true) {
			int len = readDump(br, buf, 0, buf.length);
			if (len == -1) {
				if (count > 0)
					break;

				return null;
			}

			if (len > 0) {
				count += len;
				result = concat(result, buf, len);
			}

			if (len < buf.length)
				break;
		}

		return result;
	}

	public static int readDump(BufferedReader br, byte[] out) throws IOException, InvalidLine, InvalidHexDigit {
		return readDump(br, out, 0, out.length);
	}

	public static int readDump(BufferedReader br, byte[] out, int off) throws IOException, InvalidLine, InvalidHexDigit {
		return readDump(br, out, off, out.length - off);
	}

	public static int readDump(BufferedReader br, byte[] out, int off, int len) throws IOException, InvalidLine, InvalidHexDigit {
		if (off + len > out.length)
			len = out.length - off;

		int pos = off;
		do {
			String line = br.readLine();

			if (line == null) {
				if (pos > off)
					break;

				return -1;
			}

			line = skipBlanks(line);

			if (line.equals(""))
				break;

			if (startWithHexNumber(line)) {
				int l = parseLine(line, out, pos, len - pos);
				pos += l;
				if (pos == off + len)
					break;
			}
		} while (true);

		return pos - off;
	}

	public static String skipBlanks(String line) {
		if (line == null)
			return null;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c != ' ' && c != '\t')
				return line.substring(i);
		}

		return "";
	}

	private static void skipBlanks(String line, int i[]) {
		while (i[0] < line.length() && line.charAt(i[0]) == ' ')
			i[0]++;
	}

	public static String skipBlanksLines(BufferedReader br) throws IOException {
		String line = br.readLine();
		if (line == null)
			return null;

		line = skipBlanks(line);
		while (line != null && skipBlanks(line).equals(""))
			line = br.readLine();

		return line;
	}

	private static boolean startWithHexNumber(String line) {
		String line1 = line.trim();
		if (!HexUtil.isHexdigit(line1.charAt(0)))
			return false;

		if (!HexUtil.isHexdigit(line1.charAt(1)))
			return false;

		if (!HexUtil.isHexdigit(line1.charAt(2)))
			return false;

		if (!HexUtil.isHexdigit(line1.charAt(3)))
			return false;

		return true;
	}

	public static String writeDump(byte[] buf) {
		return writeDump(buf, 0, buf.length);
	}

	public static String writeDump(byte[] buf, int off, int len) {
		if (buf == null)
			return "";

		String result = "";
		int k = off;
		int k1 = off + len;
		int l = 0;
		while (k < k1) {
			result += HexUtil.intToString(l * 16, 4) + "  ";
			int k0 = k;
			for (int j = 0; j < 16; j++)
				if (k < k1) {
					result += " " + HexUtil.intToString(buf[k], 2);
					k++;
				} else
					result += "   ";
			result += "  ";
			k = k0;
			for (int j = 0; j < 16; j++) {
				if (k >= k1)
					break;
				if (buf[k] >= 32 && buf[k] <= 126)
					result += (char) buf[k];
				else
					result += ".";
				k++;
			}
			result += "\r\n";
			l++;
		}
		result += "\r\n";

		return result;
	}

	public static String writeDump(String s) {
		return writeDump(s.getBytes(), 0, s.length());
	}

}
