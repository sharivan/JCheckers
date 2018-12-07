package jcheckers.common.logic.boards.draughts;

import java.util.List;

import common.util.Tree;
import common.util.Tree.Node;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPiece;
import jcheckers.common.logic.boards.BoardPosition;

public abstract class DraughtsPiece extends BoardPiece {

	protected static int signal(int x) {
		if (x > 0)
			return 1;

		if (x < 0)
			return -1;

		return 0;
	}

	boolean captured;

	protected DraughtsPiece(DraughtsGame game, int playerIndex) {
		super(game, playerIndex);
	}

	protected DraughtsPiece(DraughtsGame game, int playerIndex, BoardPosition position) {
		super(game, playerIndex, position);
	}

	@Override
	protected void destroy() {
		super.destroy();
	}

	protected abstract void generateCaptureList(int dr, int dc, Node<DraughtsNodePosition> node);

	@Override
	protected final void generateMoveList(List<BoardMove> moveList) {
		DraughtsGame game = getGame();

		Tree<DraughtsNodePosition> move = new Tree<>(new DraughtsNodePosition(getPosition(), isKing()));
		Node<DraughtsNodePosition> root = move.getRoot();
		generateCaptureList(0, 0, root);

		if (move.getChildCount() > 0) {
			if (!game.hasCaptures) {
				moveList.clear();
				game.hasCaptures = true;
			}

			MoveInfo info = new MoveInfo();
			game.addMovesFromTree(moveList, move, info);
		}

		if (!game.hasCaptures)
			generateNonCaptureList(moveList);
	}

	protected abstract void generateNonCaptureList(List<BoardMove> moveList);

	@Override
	public DraughtsGame getGame() {
		return (DraughtsGame) super.getGame();
	}

	protected final boolean isAhead(int dstRow) {
		if (isWhite())
			return dstRow < getPosition().getRow();

		if (isBlack())
			return dstRow > getPosition().getRow();

		return false;
	}

	public final boolean isBlack() {
		return getPlayerIndex() == DraughtsGame.BLACK;
	}

	public abstract boolean isKing();

	public final boolean isMan() {
		return !isKing();
	}

	public final boolean isRedKing() {
		return isKing() && isBlack();
	}

	public final boolean isRedMan() {
		return isMan() && isBlack();
	}

	protected abstract BoardPosition isValidSingleCapture(int dstRow, int dstCol, boolean firstCapture);

	public final boolean isWhite() {
		return getPlayerIndex() == DraughtsGame.WHITE;
	}

	public final boolean isWhiteKing() {
		return isKing() && isWhite();
	}

	public final boolean isWhiteMan() {
		return isMan() && isWhite();
	}

	protected void release() {
		getGame().release(this);
	}

}
