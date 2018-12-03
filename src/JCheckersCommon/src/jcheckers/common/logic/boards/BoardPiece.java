package jcheckers.common.logic.boards;

import java.util.ArrayList;
import java.util.List;

public abstract class BoardPiece implements Piece {

	BoardGame game;
	int playerIndex;
	BoardPosition position;
	boolean changingPosition;

	public BoardPiece(BoardGame game, int playerIndex) {
		this(game, playerIndex, null);
	}

	protected BoardPiece(BoardGame game, int playerIndex, BoardPosition position) {
		this.game = game;
		this.playerIndex = playerIndex;
		this.position = position;
	}

	protected void destroy() {
		playerIndex = -1;
		position = null;
		game = null;
	}

	protected abstract void generateMoveList(List<BoardMove> moveList);

	@Override
	public BoardGame getGame() {
		return game;
	}

	public List<BoardMove> getMoveList() {
		List<BoardMove> result = new ArrayList<>();
		generateMoveList(result);
		return result;
	}

	@Override
	public int getPlayerIndex() {
		return playerIndex;
	}

	public BoardPosition getPosition() {
		return position;
	}

	public void setPosition(BoardPosition position) {
		if (changingPosition)
			return;
		if (this.position == null && position == null)
			return;
		if (this.position != null && this.position.equals(position))
			return;

		changingPosition = true;

		game.setBoard(this.position, null);
		this.position = position;
		if (position != null)
			game.setBoard(position, this);

		changingPosition = false;
	}

	@Override
	public String toString() {
		return "BoardPiece [playerIndex=" + playerIndex + ", position=" + position + "]";
	}

}
