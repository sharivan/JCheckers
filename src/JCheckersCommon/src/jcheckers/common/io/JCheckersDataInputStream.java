package jcheckers.common.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class JCheckersDataInputStream extends FilterInputStream {

	private static final String CHARSET = "windows-1252";

	public JCheckersDataInputStream(InputStream in) {
		super(in);
	}

	private void handleIOException(IOException e) throws JCheckersIOException {
		if (in instanceof JCheckersInputStream) {
			JCheckersInputStream gin = (JCheckersInputStream) in;
			gin.throwIOException(e);
			return;
		}

		throw new JCheckersIOException(e);
	}

	public boolean readBoolean() throws JCheckersIOException {
		int i;
		try {
			i = read();
		} catch (IOException e) {
			handleIOException(e);
			return false;
		}

		return i != 0;
	}

	public byte readChar() throws JCheckersIOException {
		int i;
		try {
			i = read();
		} catch (IOException e) {
			handleIOException(e);
			return -1;
		}

		return (byte) i;
	}

	public String readEasyString() throws JCheckersIOException {
		byte[] result = new byte[65536];
		int count = 0;
		for (int j = 0; j < 65536; j++) {
			int i;
			try {
				i = read();
			} catch (IOException e) {
				handleIOException(e);
				return null;
			}
			if (i == 0)
				try {
					return new String(result, 0, count, CHARSET);
				} catch (UnsupportedEncodingException e) {
					handleIOException(e);
				}

			result[j] = (byte) (i & 0xff);
			count++;
		}

		throw new InvalidDataEasyString();
	}

	public void readFully(byte[] b) throws JCheckersIOException {
		readFully(b, 0, b.length);
	}

	public void readFully(byte[] b, int off, int len) throws JCheckersIOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();

		int n = 0;
		while (n < len) {
			int count;
			try {
				count = read(b, off + n, len - n);
			} catch (IOException e) {
				handleIOException(e);
				return;
			}
			if (count < 0)
				throw new JCheckersEOFException();

			n += count;
		}
	}

	public int readInt() throws JCheckersIOException {
		int j = 0;
		for (int k = 0; k < 4; k++)
			try {
				j |= read() << 8 * k;
			} catch (IOException e) {
				handleIOException(e);
			}

		return j;
	}

	public long readInt64() throws JCheckersIOException {
		long l = 0;
		for (int k = 0; k < 8; k++)
			try {
				l |= read() << 8 * k;
			} catch (IOException e) {
				handleIOException(e);
			}

		return l;
	}

	public short readShort() throws JCheckersIOException {
		int i = 0;
		for (int k = 0; k < 2; k++)
			try {
				i |= read() << 8 * k;
			} catch (IOException e) {
				handleIOException(e);
			}

		return (short) i;
	}

	public String readString() throws JCheckersIOException {
		int len = readUShort();
		if (len < 0)
			throw new InvalidDataString("Length " + len + " is negative.");

		if (len == 0)
			return "";

		byte[] result = new byte[len];
		for (int k = 0; k < len; k++)
			try {
				result[k] = (byte) (read() & 0xff);
			} catch (IOException e) {
				handleIOException(e);
			}

		try {
			if (read() != 0)
				throw new InvalidDataString("Null byte expected at end of data.");
		} catch (IOException e) {
			handleIOException(e);
		}

		try {
			return new String(result, 0, len, CHARSET);
		} catch (UnsupportedEncodingException e) {
			handleIOException(e);
		}

		return null;
	}

	public int readUChar() throws JCheckersIOException {
		int i;
		try {
			i = read();
		} catch (IOException e) {
			handleIOException(e);
			return -1;
		}

		return i & 0xff;
	}

	public int readUShort() throws JCheckersIOException {
		int i = 0;
		for (int k = 0; k < 2; k++)
			try {
				i |= read() << 8 * k;
			} catch (IOException e) {
				handleIOException(e);
			}

		return i & 0xffff;
	}

	public int skipBytes(int n) throws JCheckersIOException {
		int total = 0;
		int cur = 0;

		try {
			while (total < n && (cur = (int) skip(n - total)) > 0)
				total += cur;
		} catch (IOException e) {
			handleIOException(e);
		}

		return total;
	}

}