package jcheckers.common.logic.boards.draughts;

import jcheckers.common.logic.boards.BoardPosition;

class DraughtsNodePosition {

	BoardPosition position;
	int captureds;
	int capturedKings;
	boolean capturingWithKing;
	boolean startedCapturingAKing;

	DraughtsNodePosition(BoardPosition position, boolean capturingWithKing) {
		this.position = position;
		this.capturingWithKing = capturingWithKing;

		captureds = 0;
		capturedKings = 0;
		startedCapturingAKing = false;
	}

	DraughtsNodePosition(BoardPosition position, DraughtsNodePosition parent, boolean capturedAKing) {
		this.position = position;

		capturingWithKing = parent.capturingWithKing;
		captureds = parent.captureds + 1;
		capturedKings = parent.capturedKings;
		if (capturedAKing)
			capturedKings++;

		if (captureds == 1 && capturedKings == 1)
			startedCapturingAKing = true;
		else
			startedCapturingAKing = parent.startedCapturingAKing;
	}

}
