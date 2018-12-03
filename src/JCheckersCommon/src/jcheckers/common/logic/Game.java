package jcheckers.common.logic;

public abstract class Game {

	public enum StopReason {
		NOT_STOPED, NORMAL, CANCELED, RESIGN, DRAW, TIMEUP;
	}

	private int gameNumber;
	private boolean running;
	private boolean closed;
	private StopReason stopReason;

	public Game() {
		running = false;
	}

	public boolean close() {
		if (closed)
			return false;

		stop(StopReason.NORMAL);

		closed = true;

		return true;
	}

	public int getGameNumber() {
		return gameNumber;
	}

	public StopReason getStopReason() {
		return stopReason;
	}

	public boolean isCanceled() {
		return stopReason == StopReason.CANCELED;
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean start() {
		if (running)
			return false;

		gameNumber++;
		stopReason = StopReason.NOT_STOPED;
		running = true;

		return true;
	}

	public final void stop() {
		stop(StopReason.NORMAL);
	}

	public void stop(StopReason reason) {
		running = false;
		stopReason = reason;
	}

}
