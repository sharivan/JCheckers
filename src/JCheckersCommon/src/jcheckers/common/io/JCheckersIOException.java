package jcheckers.common.io;

import java.io.IOException;
import java.util.Arrays;

public class JCheckersIOException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4259914208006574465L;

	private byte[] buf = null;

	public JCheckersIOException() {
		super();
	}

	public JCheckersIOException(byte[] buf) {
		super();

		this.buf = buf != null ? Arrays.copyOf(buf, buf.length) : null;
	}

	public JCheckersIOException(byte[] buf, String message) {
		super(message);

		this.buf = buf != null ? Arrays.copyOf(buf, buf.length) : null;
	}

	public JCheckersIOException(byte[] buf, String message, Throwable cause) {
		super(message, cause);

		this.buf = buf != null ? Arrays.copyOf(buf, buf.length) : null;
	}

	public JCheckersIOException(byte[] buf, Throwable cause) {
		super(cause);

		this.buf = buf != null ? Arrays.copyOf(buf, buf.length) : null;
	}

	public JCheckersIOException(String message) {
		super(message);
	}

	public JCheckersIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public JCheckersIOException(Throwable cause) {
		super(cause);
	}

	public byte[] getBuf() {
		return buf;
	}

}
