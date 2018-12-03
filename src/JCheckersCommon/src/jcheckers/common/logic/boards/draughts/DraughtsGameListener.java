package jcheckers.common.logic.boards.draughts;

import jcheckers.common.logic.boards.BoardGameListener;

public interface DraughtsGameListener extends BoardGameListener {

	void onCapture(DraughtsPiece piece, int row, int col);

	void onPromote(DraughtsMan man, int row, int col);

}
