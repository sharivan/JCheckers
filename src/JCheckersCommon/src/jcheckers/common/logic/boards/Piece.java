package jcheckers.common.logic.boards;

public interface Piece {

	default boolean doMove(BoardMove move) {
		return getGame().doMove(move);
	}

	public BoardGame getGame();

	default BoardPlayer getPlayer() {
		return (BoardPlayer) getGame().getPlayer(getPlayerIndex());
	}

	public int getPlayerIndex();

}
