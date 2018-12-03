/**
 * 
 */
package jcheckers.common.io;

/**
 * @author SHARIVAN
 * 
 */

public class InvalidDataEasyString extends JCheckersIOException {

	/**
	 * 
	 */

	private static final long serialVersionUID = -1866334002320150868L;

	public InvalidDataEasyString() {
		super();
	}

	public InvalidDataEasyString(byte[] buf) {
		super(buf);
	}

	public InvalidDataEasyString(byte[] buf, String message) {
		super(buf, message);
	}

	public InvalidDataEasyString(byte[] buf, String message, Throwable cause) {
		super(buf, message, cause);
	}

	public InvalidDataEasyString(byte[] buf, Throwable cause) {
		super(buf, cause);
	}

	public InvalidDataEasyString(String message) {
		super(message);
	}

	public InvalidDataEasyString(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDataEasyString(Throwable cause) {
		super(cause);
	}

}
