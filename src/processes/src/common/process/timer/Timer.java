/**
 * 
 */
package common.process.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import common.process.AssyncProcessExceptionHandler;
import common.process.NonReturnableProcessWithoutArg;
import common.process.ProcessQueue;
import common.process.TimeOutException;

/**
 * @author Saddam
 * 
 */
public class Timer {

	private static int counter = 0;

	private boolean usingCreatedQueue;
	private ProcessQueue queue;
	private int interval;
	private boolean paused;

	private Vector<TimerListener> listeners;
	private AssyncProcessExceptionHandler handler;
	private NonReturnableProcessWithoutArg process;
	private int lastMarkTime;

	public Timer(ProcessQueue queue, int interval) {
		this(queue, "timer" + counter++, interval, false, null);
	}

	public Timer(ProcessQueue queue, int interval, AssyncProcessExceptionHandler handler) {
		this(queue, "timer" + counter++, interval, false, handler);
	}

	public Timer(ProcessQueue queue, int interval, boolean paused) {
		this(queue, "timer" + counter++, interval, paused, null);
	}

	public Timer(ProcessQueue queue, int interval, boolean paused, AssyncProcessExceptionHandler handler) {
		this(queue, "timer" + counter++, interval, paused, handler);
	}

	public Timer(ProcessQueue queue, String name, int interval, boolean paused) {
		this(queue, name, interval, paused, null);
	}

	public Timer(ProcessQueue queue, String name, int interval, boolean paused, AssyncProcessExceptionHandler handler) {
		if (interval <= 0)
			throw new RuntimeException("The interval shold greater than zero. Current interval = " + interval + ".");

		if (queue == null) {
			usingCreatedQueue = true;
			this.queue = new ProcessQueue("Timer " + name + " auto created process queue");
		}
		else {
			usingCreatedQueue = false;
			this.queue = queue;
		}
		
		this.interval = interval;
		this.paused = paused;
		this.handler = handler;

		listeners = new Vector<>();

		process = new NonReturnableProcessWithoutArg() {

			@Override
			public void exec() {
				List<TimerListener> listeners;
				synchronized (Timer.this) {
					if (isPaused())
						return;
					
					listeners = new ArrayList<>(Timer.this.listeners);
				}

				for (TimerListener listener : listeners)
					listener.notifyTimer(Timer.this, Timer.this.interval);
				
				synchronized (this) {
					lastMarkTime = 0;
					
					if (!isPaused())
						Timer.this.queue.setScheludedInterval(process, Timer.this.interval);
				}
			}
		};
		
		lastMarkTime = 0;

		if (!paused)
			this.queue.schedule(process, interval, 0, handler);
	}

	public synchronized boolean addListener(TimerListener listener) {
		return listeners.add(listener);
	}

	public void clearListeners() {
		listeners.clear();
	}

	public synchronized void close() {
		pause();

		listeners.clear();
		process = null;
		
		if (usingCreatedQueue)
			try {
				queue.close(true, true);
			} catch (TimeOutException e) {
				e.printStackTrace();
			}
		
		queue = null;
		handler = null;
	}

	public synchronized int getCurrentTime() {
		if (queue == null)
			return -1;

		if (!paused) {
			int elapsedTime = queue.getScheduledElapsedTime(process);
			if (elapsedTime == -1)
				return -1;
			
			return lastMarkTime + elapsedTime;
		}
			
		return lastMarkTime;
	}

	public synchronized int getInterval() {
		return interval;
	}

	public synchronized boolean isPaused() {
		return paused;
	}

	public synchronized void pause() {
		if (paused || queue == null)
			return;

		paused = true;
		int elapsedTime = queue.unschedule(process);
		if (elapsedTime == -1)
			return;
		
		lastMarkTime += elapsedTime;
	}

	public synchronized void play() {
		if (!paused || queue == null)
			return;

		paused = false;
		queue.schedule(process, interval - lastMarkTime, 0, handler);
	}

	public boolean removeListener(TimerListener listener) {
		return listeners.remove(listener);
	}

	public synchronized void reset() {
		if (queue == null)
			return;

		lastMarkTime = 0;

		if (!paused) {
			queue.unschedule(process);
			queue.schedule(process, interval, 0, handler);
		}
	}

	public synchronized void setCurrentTime(int time) {
		if (queue == null)
			return;
		
		if (time < 0)
			time = 0;
		else if (time > interval)
			time = interval;
		
		if (paused)
			lastMarkTime = time;
		else
			queue.setScheludedInterval(process, interval - time - lastMarkTime);
	}

	public synchronized void setInterval(int value) {
		if (queue == null)
			return;

		interval = value;

		if (!paused)
			queue.setScheludedInterval(process, interval);
	}

	public synchronized void setPaused(boolean value) {
		if (!value && paused)
			play();
		else if (value && !paused)
			pause();
	}

}
