package jcheckers.common.logic.boards.draughts;

import jcheckers.common.logic.boards.BoardPlayer;

public final class DraughtsPlayer extends BoardPlayer {

	DraughtsPlayer(DraughtsGame game, int index, String name) {
		super(game, index, name);
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