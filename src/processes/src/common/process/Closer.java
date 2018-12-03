package common.process;

public class Closer implements Closeable {

	public static final int DEFAULT_CLOSE_TIMEOUT = 10000; // 10 segundos

	private Closeable closeable;

	private boolean closing;

	public Closer(Closeable closeable) {
		this.closeable = closeable;

		closing = false;
	}

	@Override
	public void close() {
		synchronized (this) {
			if (isClosing() || isClosed())
				return;

			closing = true;
		}

		closeable.close();
	}

	public void close(boolean wait) throws TimeOutException {
		close(wait, DEFAULT_CLOSE_TIMEOUT, false);
	}

	public void close(boolean wait, boolean force) throws TimeOutException {
		close(wait, DEFAULT_CLOSE_TIMEOUT, force);
	}

	public void close(boolean wait, int timeout) throws TimeOutException {
		close(wait, DEFAULT_CLOSE_TIMEOUT, false);
	}

	public void close(boolean wait, int timeout, boolean force) throws TimeOutException {
		synchronized (this) {
			if (isClosed())
				return;

			if (isClosing()) {
				if (!wait)
					return;

				waitForClose(timeout, force);

				return;
			}

			closing = true;
		}

		closeable.close();
		if (wait)
			waitForClose(timeout, force);
	}

	public synchronized boolean isClosed() {
		return closeable == null;
	}

	public synchronized boolean isClosing() {
		return closing;
	}

	public synchronized boolean isOpen() {
		return !isClosing() && !isClosed();
	}

	public synchronized void stopClosing() {
		closing = false;
		closeable = null;

		notifyAll();
	}

	public void waitForClose() throws TimeOutException {
		waitForClose(DEFAULT_CLOSE_TIMEOUT, false);
	}

	public void waitForClose(boolean force) throws TimeOutException {
		waitForClose(DEFAULT_CLOSE_TIMEOUT, true);
	}

	public void waitForClose(int timeout) throws TimeOutException {
		waitForClose(timeout, false);
	}

	public void waitForClose(int timeout, boolean force) throws TimeOutException {
		synchronized (this) {
			if (isClosed())
				return;

			try {
				if (timeout == 0)
					while (!isClosed())
						wait();
				else {
					int elapsed = 0;
					long lastTime = System.currentTimeMillis();
					while (!isClosed()) {
						if (elapsed >= timeout) {
							if (force && closeable instanceof Interruptable)
								((Interruptable) closeable).interrupt();

							throw new TimeOutException();
						}

						wait(timeout - elapsed);

						long time = System.currentTimeMillis();
						elapsed = (int) (time - lastTime);
						lastTime = time;
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

}
