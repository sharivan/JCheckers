package jcheckers.common.io;

import java.io.IOException;
import java.io.OutputStream;

public class JCheckersDataOutputStream {

	private static final String CHARSET = "windows-1252";

	private OutputStream out;
	private IOExceptionHandler handler;

	public JCheckersDataOutputStream(OutputStream out) {
		this(out, null);
	}

	public JCheckersDataOutputStream(OutputStream out, IOExceptionHandler handler) {
		this.out = out;
		this.handler = handler;
	}

	protected void close() {
		try {
			out.close();
		} catch (IOException e) {
		} finally {
			out = null;
			handler = null;
		}
	}

	public final void flush() {
		flush(true);
	}

	public final void flush(boolean close) {
		try {
			flushInternal(close);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		} finally {
			if (close)
				close();
		}
	}

	protected void flushInternal(boolean close) throws IOException {
		out.flush();
	}

	public void writeBoolean(boolean v) {
		try {
			out.write(v ? 1 : 0);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

	public void writeBytes(byte[] buf) {
		writeBytes(buf, 0, buf.length);
	}

	public void writeBytes(byte[] buf, int len) {
		writeBytes(buf, 0, len);
	}

	public void writeBytes(byte[] buf, int off, int len) {
		try {
			out.write(buf, off, len);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

	public void writeChar(int v) {
		try {
			out.write(v);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

	public void writeEasyString(String s) {
		try {
			writeEasyStringInternal(s);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

	private void writeEasyStringInternal(String s) throws IOException {
		out.write(s.getBytes(CHARSET));
		out.write(0);
	}

	public void writeInt(int v) {
		try {
			out.write(v & 0xff);
			v >>= 8;
			out.write(v & 0xff);
			v >>= 8;
			out.write(v & 0xff);
			v >>= 8;
			out.write(v & 0xff);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

	public void writeInt64(long v) {
		try {
			out.write((byte) (v & 0xff));
			v >>= 8;
			out.write((byte) (v & 0xff));
			v >>= 8;
			out.write((byte) (v & 0xff));
			v >>= 8;
			out.write((byte) (v & 0xff));
			v >>= 8;
			out.write((byte) (v & 0xff));
			v >>= 8;
			out.write((byte) (v & 0xff));
			v >>= 8;
			out.write((byte) (v & 0xff));
			v >>= 8;
			out.write((byte) (v & 0xff));
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

	public void writeShort(int v) {
		try {
			writeShortInternal(v);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

	private void writeShortInternal(int v) throws IOException {
		out.write(v & 0xff);
		v >>= 8;
		out.write(v & 0xff);
	}

	public void writeString(String s) {
		try {
			int len = s.length();
			writeShortInternal(len);
			if (len > 0)
				writeEasyStringInternal(s);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

	public void writeUChar(int v) {
		try {
			out.write(v & 0xff);
		} catch (IOException e) {
			if (handler != null)
				handler.handleIOException(e);
		}
	}

}
