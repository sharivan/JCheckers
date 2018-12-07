package jcheckers.common.logic;

import common.process.ProcessQueue;
import common.process.timer.Timer;
import jcheckers.common.logic.Game.StopReason;

public abstract class Player {

	/**
	 * 
	 */
	MultiplayerGame game;
	int index;
	String name;
	boolean started;
	boolean playing;
	boolean abandoned;
	int result;

	Timer tmrTimer;
	boolean timerWasRunning;

	protected Player(MultiplayerGame game, int index, String name) {
		this.game = game;
		this.index = index;
		this.name = name;

		started = false;
		playing = false;
		result = MultiplayerGame.NO_RESULT;

		ProcessQueue queue = this.game.controller.getQueue();
		if (queue != null) {
			tmrTimer = new Timer(queue, this.game.getDefaultTime() * 1000, true);
			tmrTimer.addListener((timer, interal) -> this.game.onTimeOut());
		}
	}

	public void cancelStart() {
		started = false;
	}

	public boolean canReceiveTurn() {
		return isPlaying();
	}

	public boolean canStart() {
		return !game.isRunning();
	}

	protected void close() {
		if (tmrTimer != null)
			tmrTimer.close();

		game = null;
	}

	public int getCurrentTime() {
		if (tmrTimer == null)
			return -1;

		int t = tmrTimer.getInterval() - tmrTimer.getCurrentTime();
		return (t >= 0 ? t : 0) / 1000;
	}

	public MultiplayerGame getGame() {
		return game;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public int getResult() {
		return result;
	}

	public boolean isAbandoned() {
		return abandoned;
	}

	public boolean isDraw() {
		return result == MultiplayerGame.DRAW;
	}

	public boolean isLoser() {
		return result == MultiplayerGame.LOSS;
	}

	public boolean isMyTurn() {
		return game.isRunning() && index == game.currentTurn;
	}

	public boolean isPlaying() {
		return game.isRunning() && playing;
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isWinner() {
		return result == MultiplayerGame.WIN;
	}

	public void leave() {
		started = false;
		abandoned = true;

		if (game.isRunning())
			resign();
	}

	protected void reportDraw() {
		result = MultiplayerGame.DRAW;
	}

	protected void reportLoser() {
		result = MultiplayerGame.LOSS;
	}

	protected void reportWinner() {
		result = MultiplayerGame.WIN;
	}

	protected void reset() {
		result = MultiplayerGame.NO_RESULT;
		abandoned = false;

		if (started) {
			started = false;
			playing = true;
		}

		if (game.hasTime && tmrTimer != null) {
			tmrTimer.setInterval(game.getTime() * 1000);
			tmrTimer.reset();
		}
	}

	public boolean resign() {
		if (!isPlaying())
			return false;

		playing = false;

		if (game.getPlayingCount() < 2)
			game.stop(StopReason.RESIGN);
		else
			game.nextTurn();

		return true;
	}

	public boolean start() {
		if (!canStart())
			return false;

		started = true;

		return true;
	}

}