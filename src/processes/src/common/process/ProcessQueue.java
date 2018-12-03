/**
 * 
 */
package common.process;

import java.util.ArrayList;

/**
 * @author Saddam
 * 
 */

public class ProcessQueue implements Interruptable {

	private abstract class NonReturnableProcessDispatcher extends ProcessDispatcher {

		int interval;
		int execCount;
		AssyncProcessExceptionHandler listener;

		long createdTime;
		int execTimes;

		NonReturnableProcessDispatcher(NonReturnableProcess process, int interval, int execCount, AssyncProcessExceptionHandler listener) {
			super(process);

			this.interval = interval;
			this.execCount = execCount;
			this.listener = listener;

			createdTime = System.currentTimeMillis();
			execTimes = 0;
		}

		@Override
		void close() {
			listener = null;

			super.close();
		}

		protected abstract void doExec();

		@Override
		void exec() {
			try {
				doExec();
			} catch (RuntimeException e) {
				if (listener != null)
					try {
						listener.notifyException(e);
					} catch (Throwable e1) {
						e1.printStackTrace();
					}
				else
					synchronized (ProcessQueue.this) {
						for (ProcessQueueExceptionListener listener : exceptionListeners)
							try {
								listener.notifyException(e);
							} catch (Throwable e1) {
								e1.printStackTrace();
							}
					}
			} finally {
				synchronized (this) {
					processed = true;
					notifyAll();
				}
			}
		}

		@SuppressWarnings("unused")
		synchronized long getCreatedTime() {
			return createdTime;
		}

		synchronized int getElapsed() {
			return (int) (System.currentTimeMillis() - createdTime);
		}

		synchronized int getInterval() {
			return interval;
		}

		synchronized long getTargetTime() {
			return createdTime + interval;
		}

		@SuppressWarnings("unused")
		synchronized boolean isTargetTimeReached() {
			return System.currentTimeMillis() - createdTime >= createdTime + interval;
		}

		synchronized void reset() {
			exception = null;
			execTimes = 0;
			createdTime = System.currentTimeMillis();
		}

		synchronized void setInterval(int interval) {
			this.interval = interval;
		}

	}

	private class NonReturnableProcessWithArgDispatcher<T> extends NonReturnableProcessDispatcher {

		NonReturnableProcessWithArg<T> process;
		T arg;

		NonReturnableProcessWithArgDispatcher(NonReturnableProcessWithArg<T> process, T arg, int interval, int execCount, AssyncProcessExceptionHandler listener) {
			super(process, interval, execCount, listener);

			this.process = process;
			this.arg = arg;
		}

		@Override
		void close() {
			process = null;
			arg = null;

			super.close();
		}

		@Override
		protected void doExec() {
			process.exec(arg);
		}

	}

	private class NonReturnableProcessWithoutArgDispatcher extends NonReturnableProcessDispatcher {

		NonReturnableProcessWithoutArg process;

		NonReturnableProcessWithoutArgDispatcher(NonReturnableProcessWithoutArg process, int interval, int execCount, AssyncProcessExceptionHandler listener) {
			super(process, interval, execCount, listener);

			this.process = process;
		}

		@Override
		void close() {
			process = null;

			super.close();
		}

		@Override
		protected void doExec() {
			process.exec();
		}

	}

	private abstract class ProcessDispatcher {

		Process process;
		RuntimeException exception;
		boolean closeOnExec;
		boolean processed;

		ProcessDispatcher(Process process) {
			this.process = process;

			processed = false;
			exception = null;
			closeOnExec = true;
		}

		void close() {
			process = null;
			exception = null;
		}

		abstract void exec();

	}

	private class ReturnableProcessDispatcher<T> extends ProcessDispatcher {

		ReturnableProcess<T> process;
		T result;

		ReturnableProcessDispatcher(ReturnableProcess<T> process) {
			super(process);

			this.process = process;

			closeOnExec = false;
			result = null;
		}

		@Override
		void close() {
			super.close();

			process = null;
			result = null;
		}

		@Override
		void exec() {
			try {
				result = process.exec();
			} catch (RuntimeException e) {
				exception = e;
			} finally {
				synchronized (this) {
					processed = true;
					notifyAll();
				}
			}
		}

	}

	private static final boolean DEBUG = false;

	private static int processQueueCreateds = 0;

	private ArrayList<ProcessDispatcher> waitingRunnables;
	private Thread processThread;

	private ArrayList<ProcessQueueExceptionListener> exceptionListeners;
	private ArrayList<CloseListener> closeListeners;

	private Closer closer;
	private boolean forceClose;

	public ProcessQueue() {
		this(Thread.currentThread().getThreadGroup(), ProcessQueue.class.getSimpleName() + "[" + processQueueCreateds++ + "]");
	}

	public ProcessQueue(String name) {
		this(Thread.currentThread().getThreadGroup(), name);
	}

	public ProcessQueue(ThreadGroup group) {
		this(group, ProcessQueue.class.getSimpleName() + "[" + processQueueCreateds++ + "]");
	}

	public ProcessQueue(ThreadGroup group, String name) {
		waitingRunnables = new ArrayList<ProcessDispatcher>();

		closer = new Closer(new Interruptable() {

			@Override
			public void close() {
				scheduleInternal(null, 0, 1, null);
			}

			@Override
			public void interrupt() {
				ProcessQueue.this.interrupt();
			}

		});

		forceClose = false;

		exceptionListeners = new ArrayList<>();
		closeListeners = new ArrayList<>();

		processThread = new Thread(group, new Runnable() {

			@Override
			public void run() {
				runThread();
			}
		}, name);
		processThread.start();
	}

	public synchronized void addCloseListener(CloseListener listener) {
		closeListeners.add(listener);
	}

	public synchronized void addExceptionListener(ProcessQueueExceptionListener handler) {
		exceptionListeners.add(handler);
	}
	
	public void cancelPending() {
		if (!closer.isOpen())
			return;
		
		synchronized (this) {
			waitingRunnables.clear();
		}
	}

	@Override
	public void close() {
		closer.close();
	}

	public void close(boolean wait) throws TimeOutException {
		closer.close(wait);
	}

	public void close(boolean wait, boolean force) throws TimeOutException {
		closer.close(wait, force);
	}

	public void close(boolean wait, int timeout) throws TimeOutException {
		closer.close(wait, timeout);
	}

	public void close(boolean wait, int timeout, boolean force) throws TimeOutException {
		closer.close(wait, timeout, force);
	}

	private boolean containsANewProcess(ArrayList<ProcessDispatcher> oldWaitings) {
		for (ProcessDispatcher dispatcher : waitingRunnables)
			if (!oldWaitings.contains(dispatcher))
				return true;

		return false;
	}

	private synchronized ProcessDispatcher getDispatcher(Process process) {
		for (ProcessDispatcher dispatcher : waitingRunnables)
			synchronized (dispatcher) {
				if (process == null && dispatcher.process == null || process != null && process.equals(dispatcher.process))
					return dispatcher;
			}

		return null;
	}

	public synchronized String getName() {
		return processThread != null ? processThread.getName() : "";
	}

	@Override
	public void interrupt() {
		forceClose = true;
		processThread.interrupt();
	}

	public synchronized boolean isClosed() {
		return closer.isClosed();
	}

	public synchronized boolean isClosing() {
		return closer.isClosing();
	}

	public synchronized boolean isCurrentThread() {
		return Thread.currentThread() == processThread;
	}

	public <T> void post(NonReturnableProcessWithArg<T> process, T arg) {
		post(process, arg, 1, null);
	}

	public <T> void post(NonReturnableProcessWithArg<T> process, T arg, AssyncProcessExceptionHandler listener) {
		post(process, arg, 1, listener);
	}

	public <T> void post(NonReturnableProcessWithArg<T> process, T arg, int execCount) {
		post(process, arg, execCount, null);
	}

	public <T> void post(NonReturnableProcessWithArg<T> process, T arg, int execCount, AssyncProcessExceptionHandler listener) {
		if (!closer.isOpen())
			return;

		if (process == null)
			throw new NullPointerException("The process cant be null.");

		scheduleInternal(process, arg, 0, execCount, listener);
	}

	public void post(NonReturnableProcessWithoutArg process) {
		post(process, 1, null);
	}

	public void post(NonReturnableProcessWithoutArg process, AssyncProcessExceptionHandler listener) {
		post(process, 1, listener);
	}

	public void post(NonReturnableProcessWithoutArg process, int execCount) {
		post(process, execCount, null);
	}

	public void post(NonReturnableProcessWithoutArg process, int execCount, AssyncProcessExceptionHandler listener) {
		if (!closer.isOpen())
			return;

		if (process == null)
			throw new NullPointerException("The process cant be null.");

		scheduleInternal(process, 0, execCount, listener);
	}

	public <T> T post(ReturnableProcess<T> process) throws InterruptedException {
		try {
			return postAndWait(process, 0);
		} catch (TimeOutException e) {
		}

		return null;
	}

	public <T> T post(ReturnableProcess<T> process, int timeout) throws TimeOutException, InterruptedException {
		return postAndWait(process, timeout);
	}

	public void postAndWait(NonReturnableProcessWithoutArg process) throws InterruptedException {
		try {
			postAndWait(process, 0);
		} catch (TimeOutException e) {
		}
	}

	public void postAndWait(NonReturnableProcessWithoutArg process, int timeout) throws TimeOutException, InterruptedException {
		if (!closer.isOpen())
			return;

		if (process == null)
			throw new NullPointerException("The process cant be null.");

		postAndWaitInternal(process, timeout);
	}

	public <T> T postAndWait(ReturnableProcess<T> process) throws InterruptedException {
		try {
			return postAndWait(process, 0);
		} catch (TimeOutException e) {
		}

		return null;
	}

	public <T> T postAndWait(ReturnableProcess<T> process, int timeout) throws TimeOutException, InterruptedException {
		if (!closer.isOpen())
			return null;

		if (process == null)
			throw new NullPointerException("The process cant be null.");

		return postAndWaitInternal(process, timeout);
	}

	private void postAndWaitInternal(NonReturnableProcessWithoutArg process, int timeout) throws TimeOutException, InterruptedException {
		if (timeout < 0)
			throw new IllegalArgumentException("The timeout " + timeout + " shold be >= 0.");

		if (isCurrentThread())
			throw new RuntimeException("A process cant be posted and waited in the same process queue thread.");

		NonReturnableProcessWithoutArgDispatcher dispatcher;
		synchronized (this) {
			dispatcher = new NonReturnableProcessWithoutArgDispatcher(process, 0, 1, null);
			waitingRunnables.add(dispatcher);
			notifyAll();
		}

		waitForDispatcher(dispatcher, timeout);

		RuntimeException exception = dispatcher.exception;
		dispatcher.close();

		if (exception != null)
			throw exception;
	}

	private <T> T postAndWaitInternal(ReturnableProcess<T> process, int timeout) throws TimeOutException, InterruptedException {
		if (timeout < 0)
			throw new IllegalArgumentException("The timeout " + timeout + " shold be >= 0.");

		if (isCurrentThread())
			throw new RuntimeException("A process cant be posted and waited in the same process queue thread.");

		ReturnableProcessDispatcher<T> dispatcher;
		synchronized (this) {
			dispatcher = new ReturnableProcessDispatcher<T>(process);
			waitingRunnables.add(dispatcher);
			notifyAll();
		}

		waitForDispatcher(dispatcher, timeout);

		RuntimeException exception = dispatcher.exception;
		if (exception != null)
			throw exception;

		T result = dispatcher.result;

		dispatcher.close();

		return result;
	}

	public synchronized void removeCloseListener(CloseListener listener) {
		closeListeners.remove(listener);
	}

	public synchronized void removeCloseListeners(CloseListener listener) {
		closeListeners.remove(listener);
	}

	public synchronized void removeExceptionListener(ProcessQueueExceptionListener handler) {
		exceptionListeners.remove(handler);
	}

	private void runThread() {
		ArrayList<ProcessDispatcher> processingRunnables = new ArrayList<>();
		try {
			while (true) {
				synchronized (this) {
					while (!forceClose && waitingRunnables.size() == 0)
						wait();

					while (!forceClose && waitingRunnables.size() > 0) {
						long currentTime = System.currentTimeMillis();
						NonReturnableProcessDispatcher targetDispatcher = null;
						long targetTime = currentTime;
						ArrayList<ProcessDispatcher> items = new ArrayList<>(waitingRunnables);
						for (ProcessDispatcher dispatcher : items)
							synchronized (dispatcher) {
								if (dispatcher instanceof ReturnableProcessDispatcher) {
									processingRunnables.add(dispatcher);
									waitingRunnables.remove(dispatcher);
								} else if (dispatcher instanceof NonReturnableProcessDispatcher) {
									NonReturnableProcessDispatcher assync = (NonReturnableProcessDispatcher) dispatcher;
									if (assync.getInterval() == 0 || currentTime >= assync.getTargetTime()) {
										processingRunnables.add(assync);
										waitingRunnables.remove(assync);
										assync.execTimes++;
										if (!closer.isClosing() && assync.execCount == 0 || assync.execTimes < assync.execCount) {
											assync.closeOnExec = false;
											assync.reset();
											waitingRunnables.add(assync);
										}
									} else if (assync.getInterval() > 0 && (targetDispatcher == null || currentTime + assync.getInterval() < targetTime)) {
										targetDispatcher = assync;
										targetTime = assync.getTargetTime();
									}
								}
							}

						if (processingRunnables.size() > 0)
							break;

						ArrayList<ProcessDispatcher> oldWaitings = new ArrayList<>(waitingRunnables);
						while (!forceClose && !containsANewProcess(oldWaitings) && targetDispatcher != null && waitingRunnables.contains(targetDispatcher) && currentTime < targetTime) {
							wait(targetTime - currentTime);
							currentTime = System.currentTimeMillis();
							targetTime = targetDispatcher.getTargetTime();
						}
					}
				}

				if (forceClose)
					return;

				for (int i = 0; i < processingRunnables.size(); i++) {
					ProcessDispatcher dispatcher = processingRunnables.get(i);
					synchronized (dispatcher) {
						if (dispatcher.process == null) {
							dispatcher.processed = true;
							dispatcher.notifyAll();

							if (DEBUG)
								System.out.println(">>>Queue " + getName() + " is closing...");

							return;
						}
					}

					try {
						dispatcher.exec();
					} finally {
						if (dispatcher.closeOnExec)
							dispatcher.close();
					}

					if (forceClose)
						return;
				}
				processingRunnables.clear();
			}
		} catch (InterruptedException e) {
		} catch (Throwable e) {
			synchronized (this) {
				for (ProcessQueueExceptionListener listener : exceptionListeners)
					try {
						listener.notifyException(e);
					} catch (Throwable e1) {
						e1.printStackTrace();
					}
			}
		} finally {
			for (ProcessDispatcher dispatcher : processingRunnables)
				synchronized (dispatcher) {
					if (dispatcher.process == null)
						try {
							dispatcher.processed = true;
							dispatcher.notifyAll();
						} finally {
							dispatcher.close();
						}
				}
			processingRunnables.clear();

			ArrayList<CloseListener> listeners;
			synchronized (this) {
				for (ProcessDispatcher dispatcher : waitingRunnables)
					synchronized (dispatcher) {
						if (dispatcher.process == null)
							try {
								dispatcher.processed = true;
								dispatcher.notifyAll();
							} finally {
								dispatcher.close();
							}
					}

				listeners = new ArrayList<>(closeListeners);

				closeListeners.clear();
				waitingRunnables.clear();
			}

			try {
				for (CloseListener listener : listeners)
					listener.onClose();
			} finally {
				closer.stopClosing();

				if (DEBUG)
					System.out.println(">>>Queue " + getName() + " is closed.");
			}

			exceptionListeners.clear();
		}
	}

	public <T> int schedule(NonReturnableProcessWithArg<T> process, T arg, int time) {
		return schedule(process, arg, time, 1, null);
	}

	public <T> int schedule(NonReturnableProcessWithArg<T> process, T arg, int time, AssyncProcessExceptionHandler listener) {
		return schedule(process, arg, time, 1, listener);
	}

	public <T> int schedule(NonReturnableProcessWithArg<T> process, T arg, int time, int execCount) {
		return schedule(process, arg, time, execCount, null);
	}

	public <T> int schedule(NonReturnableProcessWithArg<T> process, T arg, int time, int execCount, AssyncProcessExceptionHandler listener) {
		if (!closer.isOpen())
			return -1;

		if (process == null)
			throw new NullPointerException("The process item canot be null.");

		return scheduleInternal(process, arg, time, execCount, listener);
	}

	public int schedule(NonReturnableProcessWithoutArg process, int time) {
		return schedule(process, time, 1, null);
	}

	public int schedule(NonReturnableProcessWithoutArg process, int time, AssyncProcessExceptionHandler listener) {
		return schedule(process, time, 1, listener);
	}

	public int schedule(NonReturnableProcessWithoutArg process, int time, int execCount) {
		return schedule(process, time, execCount, null);
	}

	public int schedule(NonReturnableProcessWithoutArg process, int time, int execCount, AssyncProcessExceptionHandler listener) {
		if (!closer.isOpen())
			return -1;

		if (process == null)
			throw new NullPointerException("The process item canot be null.");

		return scheduleInternal(process, time, execCount, listener);
	}

	public synchronized <T> int getScheduledElapsedTime(NonReturnableProcessWithArg<T> process, T arg) {
		if (!closer.isOpen())
			return -1;

		for (ProcessDispatcher dispatcher : waitingRunnables)
			synchronized (dispatcher) {
				if (!(dispatcher instanceof NonReturnableProcessWithArgDispatcher))
					continue;

				@SuppressWarnings("unchecked")
				NonReturnableProcessWithArgDispatcher<T> assync = (NonReturnableProcessWithArgDispatcher<T>) dispatcher;
				if (process.equals(assync.process) && (arg == assync.arg || arg != null && arg.equals(assync.arg)))
					return assync.getElapsed();
			}

		return -1;
	}

	public synchronized int getScheduledElapsedTime(NonReturnableProcessWithoutArg process) {
		if (!closer.isOpen())
			return -1;

		for (ProcessDispatcher dispatcher : waitingRunnables)
			synchronized (dispatcher) {
				if (!(dispatcher instanceof NonReturnableProcessWithoutArgDispatcher))
					continue;

				NonReturnableProcessWithoutArgDispatcher assync = (NonReturnableProcessWithoutArgDispatcher) dispatcher;
				if (process.equals(assync.process))
					return assync.getElapsed();
			}

		return -1;
	}
	
	public synchronized <T> void setScheludedInterval(NonReturnableProcessWithArg<T> process, T arg, int time) {
		for (ProcessDispatcher dispatcher : waitingRunnables)
			synchronized (dispatcher) {
				if (!(dispatcher instanceof NonReturnableProcessWithArgDispatcher))
					continue;

				@SuppressWarnings("unchecked")
				NonReturnableProcessWithArgDispatcher<T> assync = (NonReturnableProcessWithArgDispatcher<T>) dispatcher;
				if (process != null && assync.process != null && process.equals(assync.process) && (arg == assync.arg || arg != null && arg.equals(assync.arg))) {
					assync.setInterval(time);
					return;
				}
			}		
	}
	
	public synchronized void setScheludedInterval(NonReturnableProcessWithoutArg process, int time) {
		for (ProcessDispatcher dispatcher : waitingRunnables)
			synchronized (dispatcher) {
				if (!(dispatcher instanceof NonReturnableProcessWithoutArgDispatcher))
					continue;

				NonReturnableProcessWithoutArgDispatcher assync = (NonReturnableProcessWithoutArgDispatcher) dispatcher;
				if (process != null && assync.process != null && process.equals(assync.process)) {
					assync.setInterval(time);
					return;
				}
			}	
	}

	private synchronized <T> int scheduleInternal(NonReturnableProcessWithArg<T> process, T arg, int time, int execCount, AssyncProcessExceptionHandler listener) {
		try {
			for (ProcessDispatcher dispatcher : waitingRunnables)
				synchronized (dispatcher) {
					if (!(dispatcher instanceof NonReturnableProcessWithArgDispatcher))
						continue;

					@SuppressWarnings("unchecked")
					NonReturnableProcessWithArgDispatcher<T> assync = (NonReturnableProcessWithArgDispatcher<T>) dispatcher;
					if (process != null && assync.process != null && process.equals(assync.process) && (arg == assync.arg || arg != null && arg.equals(assync.arg))) {
						assync.setInterval(time);
						assync.execCount += execCount;
						assync.listener = listener;

						return assync.getElapsed();
					}
				}

			waitingRunnables.add(new NonReturnableProcessWithArgDispatcher<T>(process, arg, time, execCount, listener));

			return 0;
		} finally {
			notifyAll();
		}
	}

	private synchronized int scheduleInternal(NonReturnableProcessWithoutArg process, int time, int execCount, AssyncProcessExceptionHandler listener) {
		try {
			for (ProcessDispatcher dispatcher : waitingRunnables)
				synchronized (dispatcher) {
					if (!(dispatcher instanceof NonReturnableProcessWithoutArgDispatcher))
						continue;

					NonReturnableProcessWithoutArgDispatcher assync = (NonReturnableProcessWithoutArgDispatcher) dispatcher;
					if (process != null && assync.process != null && process.equals(assync.process)) {
						assync.setInterval(time);
						assync.execCount += execCount;
						assync.listener = listener;

						return assync.getElapsed();
					}
				}

			waitingRunnables.add(new NonReturnableProcessWithoutArgDispatcher(process, time, execCount, listener));

			return 0;
		} finally {
			notifyAll();
		}
	}

	public <T> void send(NonReturnableProcessWithArg<T> process, T arg) throws InterruptedException {
		try {
			send(process, arg, 0);
		} catch (TimeOutException e) {
		}
	}

	public <T> void send(NonReturnableProcessWithArg<T> process, T arg, int timeout) throws InterruptedException, TimeOutException {
		if (!closer.isOpen())
			return;

		if (process == null)
			throw new NullPointerException("The process item canot be null.");

		sendInternal(process, arg, timeout);
	}

	public void send(NonReturnableProcessWithoutArg process) throws InterruptedException {
		try {
			send(process, 0);
		} catch (TimeOutException e) {
		}
	}

	public void send(NonReturnableProcessWithoutArg process, int timeout) throws InterruptedException, TimeOutException {
		if (!closer.isOpen())
			return;

		if (process == null)
			throw new NullPointerException("The process item canot be null.");

		sendInternal(process, timeout);
	}

	public <T> T send(ReturnableProcess<T> process) throws InterruptedException {
		try {
			return send(process, 0);
		} catch (TimeOutException e) {
			return null;
		}
	}

	public <T> T send(ReturnableProcess<T> process, int timeout) throws InterruptedException, TimeOutException {
		if (!closer.isOpen())
			return null;

		if (process == null)
			throw new NullPointerException("The process item canot be null.");

		return sendInternal(process, timeout);
	}

	private <T> void sendInternal(NonReturnableProcessWithArg<T> process, T arg, int timeout) throws InterruptedException, TimeOutException {
		if (timeout < 0)
			throw new IllegalArgumentException("The timeout " + timeout + " shold be >= 0.");

		if (isCurrentThread()) {
			if (process != null)
				process.exec(arg);
			else
				forceClose = true;

			return;
		}

		NonReturnableProcessWithArgDispatcher<T> dispatcher;
		synchronized (this) {
			dispatcher = new NonReturnableProcessWithArgDispatcher<T>(process, arg, 0, 1, null);
			if (waitingRunnables.size() > 0)
				waitingRunnables.add(0, dispatcher);
			else
				waitingRunnables.add(dispatcher);
			
			notifyAll();
		}

		waitForDispatcher(dispatcher, timeout);

		RuntimeException exception = dispatcher.exception;
		dispatcher.close();

		if (exception != null)
			throw exception;
	}

	private void sendInternal(NonReturnableProcessWithoutArg process, int timeout) throws InterruptedException, TimeOutException {
		if (timeout < 0)
			throw new IllegalArgumentException("The timeout " + timeout + " shold be >= 0.");

		if (isCurrentThread()) {
			if (process != null)
				process.exec();
			else
				forceClose = true;

			return;
		}

		NonReturnableProcessWithoutArgDispatcher dispatcher;
		synchronized (this) {
			dispatcher = new NonReturnableProcessWithoutArgDispatcher(process, 0, 1, null);
			if (waitingRunnables.size() > 0)
				waitingRunnables.add(0, dispatcher);
			else
				waitingRunnables.add(dispatcher);
			
			notifyAll();
		}

		waitForDispatcher(dispatcher, timeout);

		RuntimeException exception = dispatcher.exception;
		dispatcher.close();

		if (exception != null)
			throw exception;
	}

	private <T> T sendInternal(ReturnableProcess<T> process, int timeout) throws InterruptedException, TimeOutException {
		if (timeout < 0)
			throw new IllegalArgumentException("The timeout " + timeout + " shold be >= 0.");

		if (isCurrentThread()) {
			if (process != null)
				return process.exec();

			forceClose = true;

			return null;
		}

		ReturnableProcessDispatcher<T> dispatcher;
		synchronized (this) {
			dispatcher = new ReturnableProcessDispatcher<T>(process);
			if (waitingRunnables.size() > 0)
				waitingRunnables.add(0, dispatcher);
			else
				waitingRunnables.add(dispatcher);
			
			notifyAll();
		}

		waitForDispatcher(dispatcher, timeout);

		RuntimeException exception = dispatcher.exception;
		T result = dispatcher.result;
		dispatcher.close();

		if (exception != null)
			throw exception;

		return result;
	}

	public synchronized void setName(String name) {
		if (processThread != null)
			processThread.setName(name);
	}

	public synchronized <T> int unschedule(NonReturnableProcessWithArg<T> process, T arg) {
		if (!closer.isOpen())
			return -1;

		for (ProcessDispatcher dispatcher : waitingRunnables)
			synchronized (dispatcher) {
				if (!(dispatcher instanceof NonReturnableProcessWithArgDispatcher))
					continue;

				@SuppressWarnings("unchecked")
				NonReturnableProcessWithArgDispatcher<T> assync = (NonReturnableProcessWithArgDispatcher<T>) dispatcher;
				if (process.equals(assync.process) && (arg == assync.arg || arg != null && arg.equals(assync.arg))) {
					waitingRunnables.remove(assync);

					notifyAll();

					return assync.getElapsed();
				}
			}

		return -1;
	}

	public synchronized int unschedule(NonReturnableProcessWithoutArg process) {
		if (!closer.isOpen())
			return -1;

		for (ProcessDispatcher dispatcher : waitingRunnables)
			synchronized (dispatcher) {
				if (!(dispatcher instanceof NonReturnableProcessWithoutArgDispatcher))
					continue;

				NonReturnableProcessWithoutArgDispatcher assync = (NonReturnableProcessWithoutArgDispatcher) dispatcher;
				if (process.equals(assync.process)) {
					waitingRunnables.remove(assync);

					notifyAll();

					return assync.getElapsed();
				}
			}

		return -1;
	}

	public void waitForClose() throws TimeOutException {
		closer.waitForClose();
	}

	public void waitForClose(boolean force) throws TimeOutException {
		closer.waitForClose(force);
	}

	public void waitForClose(int timeout) throws TimeOutException {
		closer.waitForClose(timeout, false);
	}

	private void waitForDispatcher(ProcessDispatcher dispatcher, int timeout) throws InterruptedException, TimeOutException {
		synchronized (dispatcher) {
			if (timeout == 0)
				while (!dispatcher.processed)
					dispatcher.wait();
			else {
				int elapsed = 0;
				long lastTime = System.currentTimeMillis();
				while (!closer.isClosed() && !dispatcher.processed) {
					if (elapsed >= timeout)
						throw new TimeOutException();

					dispatcher.wait(timeout - elapsed);

					long time = System.currentTimeMillis();
					elapsed = (int) (time - lastTime);
					lastTime = time;
				}

				if (closer.isClosed())
					throw new InterruptedException();
			}
		}
	}

	public void waitForProcess(Process process, int timeout) throws InterruptedException, TimeOutException {
		if (!closer.isOpen())
			return;

		if (process == null)
			throw new NullPointerException("The process cant be null.");

		waitForProcessInternal(process, timeout);
	}

	private void waitForProcessInternal(Process process, int timeout) throws TimeOutException, InterruptedException {
		ProcessDispatcher dispatcher = getDispatcher(process);
		if (dispatcher == null)
			return;

		waitForDispatcher(dispatcher, timeout);
	}

	/**
	 * @return quantidade de elementos a serem processados
	 */
	public synchronized int waitingCount() {
		return waitingRunnables.size();
	}

}
