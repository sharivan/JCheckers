package jcheckers.common.logic.boards.draughts;

class MoveInfo {
	int maxCaptureds;
	boolean capturingWithKing;
	int maxCapturedKings;
	boolean startedCapturingAKing;
	int movesWithoutCapturingOrPromotion;
	boolean resetMoveCounter;

	MoveInfo() {

	}

	public void reset() {
		maxCaptureds = 0;
		capturingWithKing = false;
		maxCapturedKings = 0;
		startedCapturingAKing = false;
	}
}
