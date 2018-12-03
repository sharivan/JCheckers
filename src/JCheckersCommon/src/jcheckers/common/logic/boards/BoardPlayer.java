package jcheckers.common.logic.boards;

import jcheckers.common.logic.Player;

public abstract class BoardPlayer extends Player {

	protected BoardPlayer(BoardGame game, int index, String name) {
		super(game, index, name);
	}

	@Override
	public BoardGame getGame() {
		return (BoardGame) super.getGame();
	}

	public boolean move(BoardMove move) {
		if (!isPlaying() || !isMyTurn())
			return false;

		return getGame().doMove(move);
	}

	public boolean randomMove() {
		if (!isPlaying() || !isMyTurn())
			return false;

		return getGame().doRandomMove();
	}

	@Override
	protected void reportDraw() {
		super.reportDraw();
	}

	@Override
	protected void reportLoser() {
		super.reportLoser();
	}

	@Override
	protected void reportWinner() {
		super.reportWinner();
	}

}