package common.process;

public class ProcessQueueException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5145527032762565015L;

	public ProcessQueueException() {
		super();
	}

	public ProcessQueueException(String message) {
		super(message);
	}

	public ProcessQueueException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessQueueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ProcessQueueException(Throwable cause) {
		super(cause);
	}

}
