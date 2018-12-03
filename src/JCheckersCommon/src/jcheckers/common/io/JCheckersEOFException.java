package jcheckers.common.io;

public class JCheckersEOFException extends JCheckersIOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -147108777113937558L;

	public JCheckersEOFException() {
		super();
	}

	public JCheckersEOFException(byte[] buf) {
		super(buf);
	}

	public JCheckersEOFException(byte[] buf, String message) {
		super(buf, message);
	}

	public JCheckersEOFException(byte[] buf, String message, Throwable cause) {
		super(buf, message, cause);
	}

	public JCheckersEOFException(byte[] buf, Throwable cause) {
		super(buf, cause);
	}

	public JCheckersEOFException(String message) {
		super(message);
	}

	public JCheckersEOFException(String message, Throwable cause) {
		super(message, cause);
	}

	public JCheckersEOFException(Throwable cause) {
		super(cause);
	}

}
