package jcheckers.common.logic.boards.draughts;

import java.util.List;

import common.util.Tree;
import common.util.Tree.Node;
import jcheckers.common.logic.GameListener;
import jcheckers.common.logic.Player;
import jcheckers.common.logic.boards.BoardGame;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPosition;

public final class DraughtsGame extends BoardGame {

	public static final int WHITE = 0;
	public static final int BLACK = 1;

	private DraughtsConfig config;

	private DraughtsPlayer[] players;

	private int whiteManIndex;
	private DraughtsMan[] whiteMen;
	private int whiteKingIndex;
	private DraughtsKing[] whiteKings;
	private int blackManIndex;
	private DraughtsMan[] blackMen;
	private int blackKingIndex;
	private DraughtsKing[] blackKings;

	boolean hasCaptures;
	MoveInfo info = new MoveInfo();

	public DraughtsGame(DraughtsGameController controller) {
		super(controller);

		config = DraughtsConfig.BRAZILIAN;

		players = (DraughtsPlayer[]) super.players;

		int size = getSize();
		int pieceCount = size * (size - 2) / 4;

		whiteMen = new DraughtsMan[pieceCount];
		whiteKings = new DraughtsKing[pieceCount];
		for (int i = 0; i < whiteMen.length; i++) {
			whiteMen[i] = new DraughtsMan(this, 0);
			whiteKings[i] = new DraughtsKing(this, 0);
		}
		blackMen = new DraughtsMan[pieceCount];
		blackKings = new DraughtsKing[pieceCount];
		for (int i = 0; i < blackMen.length; i++) {
			blackMen[i] = new DraughtsMan(this, 1);
			blackKings[i] = new DraughtsKing(this, 1);
		}
	}

	DraughtsKing acquireKing(int playerIndex) {
		if (playerIndex == WHITE)
			return whiteKings[whiteKingIndex++];

		return blackKings[blackKingIndex++];
	}

	DraughtsMan acquireMan(int playerIndex) {
		if (playerIndex == WHITE)
			return whiteMen[whiteManIndex++];

		return blackMen[blackManIndex++];
	}

	void addMovesFromTree(List<BoardMove> moveList, Tree<DraughtsNodePosition> tree, MoveInfo info) {
		for (Node<DraughtsNodePosition> node : tree) {
			DraughtsNodePosition dp = node.getValue();

			if (hasMaximumCapture())
				if (dp.captureds > info.maxCaptureds) {
					info.maxCaptureds = dp.captureds;
					info.capturingWithKing = dp.capturingWithKing;
					info.maxCapturedKings = dp.capturedKings;
					info.startedCapturingAKing = dp.startedCapturingAKing;
					moveList.clear();
				} else if (dp.captureds < info.maxCaptureds)
					continue;

			if (hasMaximumCaptureWithKings())
				if (!info.capturingWithKing && dp.capturingWithKing) {
					info.capturingWithKing = true;
					info.maxCapturedKings = dp.capturedKings;
					info.startedCapturingAKing = dp.startedCapturingAKing;
					moveList.clear();
				} else if (info.capturingWithKing && !dp.capturingWithKing)
					continue;

			if (hasMaximumCaptureAmountOfKings())
				if (dp.capturedKings > info.maxCapturedKings) {
					info.capturingWithKing = dp.capturingWithKing;
					info.maxCapturedKings = dp.capturedKings;
					info.startedCapturingAKing = dp.startedCapturingAKing;
					moveList.clear();
				} else if (dp.capturedKings < info.maxCapturedKings)
					continue;

			if (hasMaximumCaptureAmountOfKingsStartingCapturingAKing())
				if (!info.startedCapturingAKing && dp.startedCapturingAKing) {
					info.capturingWithKing = dp.capturingWithKing;
					info.maxCapturedKings = dp.capturedKings;
					info.startedCapturingAKing = true;
					moveList.clear();
				} else if (info.startedCapturingAKing && !dp.startedCapturingAKing)
					continue;

			addToMoveList(node, moveList);
		}
	}

	private void addToMoveList(Node<DraughtsNodePosition> node, List<BoardMove> moveList) {
		List<DraughtsNodePosition> positions = node.getPathFromRoot();
		BoardPosition[] p = new BoardPosition[positions.size()];
		for (int i = 0; i < positions.size(); i++)
			p[i] = positions.get(i).position;

		BoardMove move = new BoardMove(p);
		moveList.add(move);
	}

	public boolean canManCaptureKings() {
		return config.canManCaptureKings();
	}

	public boolean canManMakeBackCaptures() {
		return config.canManMakeBackCaptures();
	}

	@Override
	public boolean close() {
		if (!super.close())
			return false;

		for (int i = 0; i < blackMen.length; i++) {
			if (blackMen[i] != null) {
				blackMen[i].destroy();
				blackMen[i] = null;
			}
			if (blackKings[i] != null) {
				blackKings[i].destroy();
				blackKings[i] = null;
			}
			if (whiteMen[i] != null) {
				whiteMen[i].destroy();
				whiteMen[i] = null;
			}
			if (whiteKings[i] != null) {
				whiteKings[i].destroy();
				whiteKings[i] = null;
			}
		}

		return true;
	}

	@Override
	protected Player createPlayer(int playerIndex, String name) {
		return new DraughtsPlayer(this, playerIndex, name);
	}

	@Override
	protected Player[] createPlayers() {
		return new DraughtsPlayer[2];
	}

	@Override
	protected void doSingleMove(int srcRow, int srcCol, int dstRow, int dstCol, boolean hasMore) {
		DraughtsPiece piece = getBoardInternal(srcRow, srcCol);
		setBoardInternal(srcRow, srcCol, null);
		setBoardInternal(dstRow, dstCol, piece);
		if (piece instanceof DraughtsMan) {
			DraughtsMan man = (DraughtsMan) piece;
			if (man.gotLastRow())
				if (hasMore) {
					if (manCanBecomeKingDuringCapture()) {
						DraughtsKing king = man.promote();
						setBoardInternal(dstRow, dstCol, king);

						for (GameListener listener : listeners)
							if (listener != null && listener instanceof DraughtsGameListener)
								try {
									((DraughtsGameListener) listener).onPromote(man, dstRow, dstCol);
								} catch (Throwable e) {
									e.printStackTrace();
								}
					}
				} else {
					DraughtsKing king = man.promote();
					setBoardInternal(dstRow, dstCol, king);

					for (GameListener listener : listeners)
						if (listener != null && listener instanceof DraughtsGameListener)
							try {
								((DraughtsGameListener) listener).onPromote(man, dstRow, dstCol);
							} catch (Throwable e) {
								e.printStackTrace();
							}
				}
		}

		for (int i = 1; i < Math.abs(dstRow - srcRow); i++) {
			int capturedRow = dstRow > srcRow ? srcRow + i : srcRow - i;
			int capturedCol = dstCol > srcCol ? srcCol + i : srcCol - i;
			DraughtsPiece captured = getBoardInternal(capturedRow, capturedCol);
			setBoardInternal(capturedRow, capturedCol, null);

			for (GameListener listener : listeners)
				if (listener != null && listener instanceof DraughtsGameListener)
					try {
						((DraughtsGameListener) listener).onCapture(captured, capturedRow, capturedCol);
					} catch (Throwable e) {
						e.printStackTrace();
					}
		}
	}

	@Override
	protected void generateMoveListImpl() {
		hasCaptures = false;
		info.reset();
		super.generateMoveListImpl();
	}

	@Override
	protected DraughtsPiece getBoardInternal(BoardPosition position) {
		return (DraughtsPiece) super.getBoardInternal(position);
	}

	@Override
	protected DraughtsPiece getBoardInternal(int row, int col) {
		return (DraughtsPiece) super.getBoardInternal(row, col);
	}

	@Override
	public final int getColCount() {
		return getSize();
	}

	public DraughtsConfig getConfig() {
		return config;
	}

	public int getInitialColor() {
		return config.getInitialColor();
	}

	@Override
	public final int getInitialTurn() {
		return config.getInitialColor();
	}

	@Override
	public int getMaxRounds() {
		return 0;
	}

	@Override
	public DraughtsPlayer getPlayer(int playerIndex) {
		return (DraughtsPlayer) super.getPlayer(playerIndex);
	}

	@Override
	public final int getRowCount() {
		return getSize();
	}

	public int getSize() {
		return config.getSize();
	}

	public boolean hasFlyingKings() {
		return config.hasFlyingKings();
	}

	public boolean hasMaximumCapture() {
		return config.hasMaximumCapture();
	}

	public boolean hasMaximumCaptureAmountOfKings() {
		return config.hasMaximumCaptureAmountOfKings();
	}

	public boolean hasMaximumCaptureAmountOfKingsStartingCapturingAKing() {
		return config.hasMaximumCaptureAmountOfKingsStartingCapturingAKing();
	}

	public boolean hasMaximumCaptureWithKings() {
		return config.hasMaximumCaptureWithKings();
	}

	public boolean isMirrored() {
		return config.isMirrored();
	}

	@Override
	public boolean isValidPos(int row, int col) {
		return super.isValidPos(row, col) && (isMirrored() ? (row + col & 1) != 0 : (row + col & 1) == 0);
	}

	public boolean manCanBecomeKingDuringCapture() {
		return config.canManCanBecomeKingDuringCapture();
	}

	void release(DraughtsPiece piece) {
		if (piece.isWhiteMan())
			whiteManIndex--;
		else if (piece.isWhiteKing())
			whiteKingIndex--;
		else if (piece.isRedMan())
			blackManIndex--;
		else if (piece.isRedKing())
			blackKingIndex--;
	}

	protected void setBoardInternal(BoardPosition position, DraughtsPiece piece) {
		super.setBoardInternal(position, piece);
	}

	protected void setBoardInternal(int row, int col, DraughtsPiece piece) {
		super.setBoardInternal(row, col, piece);
	}

	public void setConfig(DraughtsConfig config) {
		this.config = config;
	}

	@Override
	protected void setupBoard() {
		blackManIndex = 0;
		blackKingIndex = 0;
		whiteManIndex = 0;
		whiteKingIndex = 0;

		for (int i = 0; i < blackMen.length; i++) {
			blackMen[i].setPosition(null);
			blackKings[i].setPosition(null);
			whiteMen[i].setPosition(null);
			whiteKings[i].setPosition(null);
		}

		int size = getSize();
		for (int row = 0; row < size / 2 - 1; row++)
			for (int col = 0; col < size; col++) {
				if (!isValidPos(row, col))
					continue;

				setBoardInternal(row, col, acquireMan(BLACK));
			}

		for (int row = size / 2 - 1; row < size / 2 + 1; row++)
			for (int col = 0; col < size; col++) {
				if (!isValidPos(row, col))
					continue;

				setBoardInternal(row, col, null);
			}

		for (int row = size / 2 + 1; row < size; row++)
			for (int col = 0; col < size; col++) {
				if (!isValidPos(row, col))
					continue;

				setBoardInternal(row, col, acquireMan(WHITE));
			}
	}

	@Override
	public void stop(StopReason reason) {
		if (!isRunning())
			return;

		if (reason != StopReason.CANCELED)
			if (reason == StopReason.RESIGN)
				for (DraughtsPlayer player : players)
					if (!player.isPlaying())
						player.reportLoser();
					else
						player.reportWinner();
			else if (reason == StopReason.DRAW)
				for (DraughtsPlayer player : players)
					player.reportDraw();
			else
				for (DraughtsPlayer player : players)
					if (player.isMyTurn())
						player.reportLoser();
					else
						player.reportWinner();

		super.stop(reason);
	}

	@Override
	protected String turnToStr(int currentTurn) {
		switch (currentTurn) {
			case DraughtsConfig.BLACK:
				return "Black";
			case DraughtsConfig.WHITE:
				return "White";
		}

		return "?";
	}

}
