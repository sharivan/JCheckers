/**
 * 
 */
package common.process;

import javax.security.auth.Destroyable;

/**
 * @author saddam
 * 
 */

public class ProcessPool implements Destroyable {

	static int DEFAULT_POOL_SIZE = 4;

	private boolean destroyed = false;

	private int size;
	private ProcessQueue[] process;
	private ThreadGroup myGroup;

	public ProcessPool() {
		this(Thread.currentThread().getThreadGroup(), DEFAULT_POOL_SIZE);
	}

	public ProcessPool(int size) {
		this(Thread.currentThread().getThreadGroup(), size);
	}

	public ProcessPool(ThreadGroup group) {
		this(group, DEFAULT_POOL_SIZE);
	}

	public ProcessPool(ThreadGroup group, int size) {
		if (size <= 0)
			throw new ArrayIndexOutOfBoundsException("The size of pool can't be <= 0.");

		this.size = size;

		myGroup = new ThreadGroup(group, ProcessPool.class.getName());
		process = new ProcessQueue[size];
		for (int i = 0; i < size; i++)
			process[i] = new ProcessQueue(myGroup, "Process Pool Queue [" + i + "]");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.security.auth.Destroyable#destroy()
	 */
	@Override
	public synchronized void destroy() {
		if (destroyed)
			return;

		for (int i = 0; i < process.length; i++) {
			ProcessQueue queue = process[i];
			if (queue != null)
				try {
					queue.close(true, true);
				} catch (TimeOutException e) {
					System.err.println("WARNING: The queue  " + queue.getName() + " was forced closed.");
				} finally {
					process[i] = null;
				}
		}

		myGroup = null;
		destroyed = true;
	}

	public synchronized ProcessQueue getProcessQueue() {
		ProcessQueue result = process[0];
		int minSize = result.waitingCount();
		if (minSize == 0)
			return result;
		for (int i = 1; i < process.length; i++) {
			ProcessQueue result1 = process[i];
			int size = result.waitingCount();
			if (size == 0)
				return result1;
			if (size < minSize) {
				result = result1;
				minSize = size;
			}
		}
		return result;
	}

	/**
	 * @return the size
	 */
	public synchronized int getSize() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.security.auth.Destroyable#isDestroyed()
	 */
	@Override
	public synchronized boolean isDestroyed() {
		return destroyed;
	}

	public <T> void post(NonReturnableProcessWithArg<T> process, T arg) {
		ProcessQueue queue = getProcessQueue();
		queue.post(process, arg);
	}

	public <T> void post(NonReturnableProcessWithArg<T> process, T arg, AssyncProcessExceptionHandler listener) {
		ProcessQueue queue = getProcessQueue();
		queue.post(process, arg, listener);
	}

	public void post(NonReturnableProcessWithoutArg process) {
		ProcessQueue queue = getProcessQueue();
		queue.post(process);
	}

	public void post(NonReturnableProcessWithoutArg process, AssyncProcessExceptionHandler listener) {
		ProcessQueue queue = getProcessQueue();
		queue.post(process, listener);
	}

	public <T> void send(NonReturnableProcessWithArg<T> process, T arg) throws InterruptedException {
		ProcessQueue queue = getProcessQueue();

		queue.send(process, arg);
	}

	public void send(NonReturnableProcessWithoutArg process) throws InterruptedException {
		ProcessQueue queue = getProcessQueue();

		queue.send(process);
	}

	public <T> T send(ReturnableProcess<T> process) throws InterruptedException {
		ProcessQueue queue = getProcessQueue();

		return queue.send(process);
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public synchronized void setSize(int size) {
		if (size < 0)
			throw new ArrayIndexOutOfBoundsException("The size is negative");
		if (this.size == size)
			return;
		ProcessQueue[] temp = new ProcessQueue[size];
		if (this.size > size) {
			for (int i = size; i < this.size; i++)
				try {
					process[i].close(true, true);
				} catch (TimeOutException e) {
					System.err.println("WARNING: The queue " + process[i].getName() + " was forced closed.");
				}
			System.arraycopy(process, 0, temp, 0, size);
		} else if (this.size < size) {
			System.arraycopy(process, 0, temp, 0, this.size);
			for (int i = this.size; i < size; i++)
				temp[i] = new ProcessQueue(myGroup, "Process Pool Queue [" + i + "]");
		}
		process = temp;
		this.size = size;
	}

}
