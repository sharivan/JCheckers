package jcheckers.common.io;

public class InvalidHeader extends JCheckersIOException {

	/**
	 * 
	 */

	private static final long serialVersionUID = -2173824410455142806L;

	public InvalidHeader() {
		super();
	}

	public InvalidHeader(byte[] buf) {
		super(buf);
	}

	public InvalidHeader(byte[] buf, int trueHeader, int header) {
		super(buf, "Invalid Net Header " + trueHeader + "!=" + header);
	}

	public InvalidHeader(byte[] buf, int trueHeader, int header, Throwable cause) {
		super(buf, "Invalid Net Header " + trueHeader + "!=" + header, cause);
	}

	public InvalidHeader(byte[] buf, String message) {
		super(buf, message);
	}

	public InvalidHeader(byte[] buf, String message, Throwable cause) {
		super(buf, message, cause);
	}

	public InvalidHeader(byte[] buf, Throwable cause) {
		super(buf, cause);
	}

	public InvalidHeader(int trueHeader, int header) {
		super("Invalid Net Header " + trueHeader + "!=" + header);
	}

	public InvalidHeader(int trueHeader, int header, Throwable cause) {
		super("Invalid Net Header " + trueHeader + "!=" + header, cause);
	}

	public InvalidHeader(String message) {
		super(message);
	}

	public InvalidHeader(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidHeader(Throwable cause) {
		super(cause);
	}

}
