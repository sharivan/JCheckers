/**
 * 
 */
package common.util;

/**
 * @author Saddam
 * 
 */

public class InvalidHexDigit extends Exception {

	/**
	 * 
	 */

	private static final long serialVersionUID = -7807968642892200910L;

	protected InvalidHexDigit() {
	}

	/**
	 * @param digit
	 */
	public InvalidHexDigit(int digit) {
		super("Dígito hexadecimal " + (char) digit + " inválido");
	}

}
