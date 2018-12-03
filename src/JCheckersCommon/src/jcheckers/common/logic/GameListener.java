package jcheckers.common.logic;

import jcheckers.common.logic.Game.StopReason;

public interface GameListener {

	void onChangeTurn(int turn);

	void onNextRound(int round);

	void onRotateLeft();

	void onRotateRight();

	void onStart();

	void onStarted();

	void onStop(StopReason reason);

	void onSwap(int index1, int index2);

}
