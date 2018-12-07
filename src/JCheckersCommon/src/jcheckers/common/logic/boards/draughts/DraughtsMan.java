package jcheckers.common.logic.boards.draughts;

import java.util.List;

import common.util.Tree.Node;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPosition;

public final class DraughtsMan extends DraughtsPiece implements Man {

	protected DraughtsMan(DraughtsGame game, int playerIndex) {
		super(game, playerIndex);
	}

	protected DraughtsMan(DraughtsGame game, int playerIndex, BoardPosition position) {
		super(game, playerIndex, position);
	}

	@Override
	protected void generateCaptureList(int dx, int dy, Node<DraughtsNodePosition> node) {
		DraughtsGame game = getGame();
		if (game == null)
			return;

		BoardPosition position = getPosition();
		if (position == null)
			return;

		int srcRow = position.getRow();
		int srcCol = position.getCol();
		for (int dx1 = -1; dx1 <= 1; dx1 += 2) {
			int dstRow = srcRow + 2 * dx1;
			if (!isAhead(dstRow))
				if (!game.canManMakeBackCaptures())
					continue;

			for (int dy1 = -1; dy1 <= 1; dy1 += 2) {
				int dstCol = srcCol + 2 * dy1;
				if (!game.isValidPos(dstRow, dstCol))
					continue;

				DraughtsPiece dstPiece = game.getBoardInternal(dstRow, dstCol);
				if (dstPiece != null)
					continue;

				BoardPosition capturedPosition = isValidSingleCapture(dstRow, dstCol, node.getParent() == null);
				if (capturedPosition != null) {
					int capturedRow = capturedPosition.getRow();
					int capturedCol = capturedPosition.getCol();
					DraughtsPiece captured = game.getBoardInternal(capturedRow, capturedCol);
					captured.captured = true;
					game.setBoardInternal(srcRow, srcCol, null);
					game.setBoardInternal(dstRow, dstCol, this);

					Node<DraughtsNodePosition> node1 = node.addChild(new DraughtsNodePosition(new BoardPosition(dstRow, dstCol), node.getValue(), captured.isKing()));

					if (gotLastRow()) {
						if (game.manCanBecomeKingDuringCapture()) {
							DraughtsPiece piece = promote();
							game.setBoardInternal(dstRow, dstCol, piece);
							piece.generateCaptureList(signal(dx1), signal(dy1), node1);
							piece.release();
						} else if (!game.stopCapturingAtLastRow())
							generateCaptureList(dstRow, dstCol, node1);
					} else
						generateCaptureList(dstRow, dstCol, node1);

					captured.captured = false;
					game.setBoardInternal(dstRow, dstCol, null);
					game.setBoardInternal(srcRow, srcCol, this);
				}
			}
		}
	}

	@Override
	protected void generateNonCaptureList(List<BoardMove> moveList) {
		DraughtsGame game = getGame();
		if (game == null)
			return;

		BoardPosition position = getPosition();
		if (position == null)
			return;

		int srcRow = position.getRow();
		int srcCol = position.getCol();
		for (int dx1 = -1; dx1 <= 1; dx1 += 2) {
			int dstRow = srcRow + dx1;
			if (!isAhead(dstRow))
				continue;

			for (int dy1 = -1; dy1 <= 1; dy1 += 2) {
				int dstCol = srcCol + dy1;
				if (game.isValidPos(dstRow, dstCol) && game.getBoardInternal(dstRow, dstCol) == null)
					moveList.add(new BoardMove(srcRow, srcCol, dstRow, dstCol));
			}
		}
	}

	public boolean gotLastRow() {
		int dstRow = getPosition().getRow();
		if (isWhite() && dstRow == 0)
			return true;
		if (isBlack() && dstRow == getGame().getSize() - 1)
			return true;

		return false;
	}

	@Override
	public boolean isKing() {
		return false;
	}

	@Override
	protected BoardPosition isValidSingleCapture(int dstRow, int dstCol, boolean firstCapture) {
		DraughtsGame game = getGame();
		if (game == null)
			return null;

		BoardPosition position = getPosition();
		if (position == null)
			return null;

		int srcRow = position.getRow();
		int srcCol = position.getCol();

		if (!isAhead(dstRow))
			if (!game.canManMakeBackCaptures())
				return null;

		int capturedRow = (srcRow + dstRow) / 2;
		int capturedCol = (srcCol + dstCol) / 2;

		DraughtsPiece capturedPiece = game.getBoardInternal(capturedRow, capturedCol);
		if (capturedPiece == null || capturedPiece.captured)
			return null;

		if (capturedPiece.getPlayerIndex() == getPlayerIndex())
			return null;

		if (capturedPiece.isKing() && !game.canManCaptureKings())
			return null;

		return capturedPiece.getPosition();
	}

	public DraughtsKing promote() {
		DraughtsKing king = getGame().acquireKing(getPlayerIndex());
		king.setPosition(getPosition());
		return king;
	}

	@Override
	public String toString() {
		switch (getPlayerIndex()) {
			case DraughtsGame.BLACK:
				return "b";
			case DraughtsGame.WHITE:
				return "w";
		}

		return "?";
	}

}
