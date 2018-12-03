package jcheckers.common.logic.boards.draughts;

import java.util.List;

import common.util.Tree.Node;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPosition;

public class DraughtsKing extends DraughtsPiece implements King {

	private static boolean generateNonCaptureListForDirection(DraughtsGame game, BoardPosition position, int dx, int dy, List<BoardMove> moveList) {
		int srcRow = position.getRow();
		int srcCol = position.getCol();

		int dstRow = srcRow + dx;
		int dstCol = srcCol + dy;

		if (!game.isValidPos(dstRow, dstCol))
			return false;

		if (game.getBoardInternal(dstRow, dstCol) != null)
			return false;

		moveList.add(new BoardMove(srcRow, srcCol, dstRow, dstCol));
		return true;
	}

	protected DraughtsKing(DraughtsGame game, int playerIndex) {
		super(game, playerIndex);
	}

	protected DraughtsKing(DraughtsGame game, int playerIndex, BoardPosition position) {
		super(game, playerIndex, position);
	}

	@Override
	protected void generateCaptureList(int dr, int dc, Node<DraughtsNodePosition> node) {
		DraughtsGame game = getGame();
		if (game == null)
			return;

		BoardPosition position = getPosition();
		if (position == null)
			return;

		int srcRow = position.getRow();
		int srcCol = position.getCol();

		int size = game.getSize();
		for (int dr1 = -srcRow; dr1 < size - srcRow; dr1++) {
			if (dr1 == 0 || dr1 == -1 || dr1 == 1)
				continue;

			int dstRow = srcRow + dr1;
			if ((dstRow < srcRow - 2 || dstRow > srcRow + 2) && !game.hasFlyingKings())
				continue;

			for (int j = -1; j < 2; j += 2) {
				int dc1 = j * dr1;
				int dstCol = srcCol + dc1;
				if (!game.isValidPos(dstRow, dstCol))
					continue;

				DraughtsPiece dstPiece = game.getBoardInternal(dstRow, dstCol);
				if (dstPiece != null)
					continue;

				int sdr = signal(dr1);
				int sdc = signal(dc1);
				if (sdr == -dr && sdc == -dc)
					continue;

				BoardPosition capturedPos = isValidSingleCapture(dstRow, dstCol, dr == 0);
				if (capturedPos != null) {
					DraughtsPiece captured = game.getBoardInternal(capturedPos);
					captured.captured = true;

					Node<DraughtsNodePosition> node1 = node.addChild(new DraughtsNodePosition(new BoardPosition(dstRow, dstCol), node.getValue(), captured.isKing()));

					game.setBoardInternal(capturedPos, captured);
					game.setBoardInternal(srcRow, srcCol, null);
					game.setBoardInternal(dstRow, dstCol, this);
					generateCaptureList(sdr, sdc, node1);
					game.setBoardInternal(capturedPos, captured);
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

		if (generateNonCaptureListForDirection(game, position, 1, 1, moveList))
			if (game.hasFlyingKings()) {
				int i = 2;
				while (generateNonCaptureListForDirection(game, position, 1 * i, 1 * i, moveList))
					i++;
			}

		if (generateNonCaptureListForDirection(game, position, 1, -1, moveList))
			if (game.hasFlyingKings()) {
				int i = 2;
				while (generateNonCaptureListForDirection(game, position, 1 * i, -1 * i, moveList))
					i++;
			}

		if (generateNonCaptureListForDirection(game, position, -1, 1, moveList))
			if (game.hasFlyingKings()) {
				int i = 2;
				while (generateNonCaptureListForDirection(game, position, -1 * i, 1 * i, moveList))
					i++;
			}

		if (generateNonCaptureListForDirection(game, position, -1, -1, moveList))
			if (game.hasFlyingKings()) {
				int i = 2;
				while (generateNonCaptureListForDirection(game, position, -1 * i, -1 * i, moveList))
					i++;
			}
	}

	@Override
	public boolean isKing() {
		return true;
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

		int n1 = dstRow - srcRow;
		int n2 = dstCol - srcCol;
		int N = Math.abs(n1);
		assert N == Math.abs(n2);
		if (N > 2 && !game.hasFlyingKings())
			return null;

		BoardPosition result = null;
		for (int k = 1; k < N; k++) {
			int capturedRow = n1 > 0 ? srcRow + k : srcRow - k;
			int capturedCol = n2 > 0 ? srcCol + k : srcCol - k;

			DraughtsPiece capturedPiece = game.getBoardInternal(capturedRow, capturedCol);
			if (capturedPiece == null)
				continue;

			if (result != null)
				return null;

			if (capturedPiece.captured)
				return null;

			if (capturedPiece.getPlayerIndex() == getPlayerIndex())
				return null;

			result = new BoardPosition(capturedRow, capturedCol);
		}

		return result;
	}

	@Override
	public String toString() {
		switch (getPlayerIndex()) {
			case DraughtsGame.BLACK:
				return "B";
			case DraughtsGame.WHITE:
				return "W";
		}

		return "?";
	}

}
