package jcheckers.common.logic.boards;

import jcheckers.common.logic.GameListener;

public interface BoardGameListener extends GameListener {

	void onMove(BoardMove move);

	void onPause(int time);

	void onPauseTimeOut();

	void onResume();

	void onSingleMove(BoardPosition src, BoardPosition dst);

	void onUndoLastMove();

	void onSetBoard(int row, int col, BoardPiece piece);

}
