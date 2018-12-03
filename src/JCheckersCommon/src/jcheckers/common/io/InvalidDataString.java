/**
 * 
 */
package jcheckers.common.io;

/**
 * @author SHARIVAN
 * 
 */

public class InvalidDataString extends JCheckersIOException {

	/**
	 * 
	 */

	private static final long serialVersionUID = 1331836310325024546L;

	public InvalidDataString() {
		super();
	}

	public InvalidDataString(byte[] buf) {
		super(buf);
	}

	public InvalidDataString(byte[] buf, String message) {
		super(buf, message);
	}

	public InvalidDataString(byte[] buf, String message, Throwable cause) {
		super(buf, message, cause);
	}

	public InvalidDataString(byte[] buf, Throwable cause) {
		super(buf, cause);
	}

	public InvalidDataString(String message) {
		super(message);
	}

	public InvalidDataString(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDataString(Throwable cause) {
		super(cause);
	}

}
