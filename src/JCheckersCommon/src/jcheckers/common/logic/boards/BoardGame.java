package jcheckers.common.logic.boards;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.process.timer.Timer;
import common.util.RandomUtil;
import jcheckers.common.logic.GameListener;
import jcheckers.common.logic.MultiplayerGame;

public abstract class BoardGame extends MultiplayerGame {

	public static final int CREATED = 0;
	public static final int RUNNING = 1;
	public static final int STOPED = 3;
	public static final int PAUSED = 5;

	public static final int DEFAULT_PAUSE_TIME = 10;

	protected BoardPiece[][] board;

	protected BoardMove partialMove;
	protected List<BoardMove> moveHistory;
	protected List<BoardMove> moveList;

	private int state;

	private Timer tmrPause;

	protected BoardPlayer[] players;
	protected boolean drawRequest;

	private boolean autoDraw;

	public BoardGame(BoardGameController controller) {
		super(controller);

		autoDraw = true;

		moveHistory = new ArrayList<>();
		moveList = new ArrayList<>();

		players = (BoardPlayer[]) super.players;

		tmrPause = new Timer(controller.getQueue(), DEFAULT_PAUSE_TIME * 1000, true);
		tmrPause.addListener((timer, interal) -> onPauseTimeOut());

		state = CREATED;
	}

	public void acceptDraw() {
		stop(StopReason.DRAW);
	}

	@Override
	protected void afterNextTurn() {
		generateMoveList();

		if (!hasMoves())
			stop();
	}

	@Override
	protected void afterPreviousTurn() {
		generateMoveList();

		if (!hasMoves())
			stop();
	}

	@Override
	protected void afterStart() {
		super.afterStart();

		state = RUNNING;
	}

	@Override
	protected boolean beforeStart() {
		if (!super.beforeStart())
			return false;

		drawRequest = false;

		board = new BoardPiece[getColCount()][getRowCount()];
		setupBoard();

		return true;
	}

	@Override
	public boolean close() {
		if (!super.close())
			return false;

		if (board != null)
			for (int row = 0; row < getRowCount(); row++)
				for (int col = 0; col < getColCount(); col++)
					if (board[col][row] != null) {
						board[col][row].destroy();
						board[col][row] = null;
					}

		moveHistory.clear();
		moveList.clear();

		tmrPause.close();

		return true;
	}

	public final boolean doMove(BoardMove move) {
		return doMove(move, true, false);
	}

	public final boolean doMove(BoardMove move, boolean acceptPartialMove) {
		return doMove(move, true, acceptPartialMove);
	}

	public final boolean doMove(BoardMove move, boolean changeTurnOnComplete, boolean acceptPartialMove) {
		if (drawRequest || isPaused())
			return false;

		boolean containsFullMove = moveList.contains(move);
		boolean containsPartMove = false;
		if (!containsFullMove)
			containsPartMove = isValidFirstMoves(move);

		if (!containsFullMove && !containsPartMove)
			return false;

		if (containsPartMove && !acceptPartialMove)
			return false;

		doMoveInternal(move);

		if (containsFullMove) {
			if (partialMove != null) {
				move = new BoardMove(partialMove, move);
				partialMove = null;
			}
				
			moveHistory.add(move);
			
			for (GameListener listener : listeners)
				if (listener != null && listener instanceof BoardGameListener)
					try {
						((BoardGameListener) listener).onMove(move);
					} catch (Throwable e) {
						e.printStackTrace();
					}

			moveList.clear();

			if (changeTurnOnComplete)
				nextTurn();
			else
				pauseAllTimers();
		} else {
			if (partialMove == null)
				partialMove = move;
			else
				partialMove = new BoardMove(partialMove, move);
			
			ArrayList<BoardMove> ml = new ArrayList<>(moveList);
			moveList.clear();
			for (int i = 0; i < ml.size(); i++) {
				BoardMove move1 = ml.get(i);
				if (move1.startWith(move))
					moveList.add(move1.subMove(move.count() - 1));
			}
		}

		return true;
	}

	protected final void doMoveInternal(BoardMove move) {
		for (int i = 0; i < move.count() - 1; i++) {
			BoardPosition src = move.get(i);
			int srcRow = src.getRow();
			int srcCol = src.getCol();
			BoardPosition dst = move.get(i + 1);
			int dstRow = dst.getRow();
			int dstCol = dst.getCol();

			for (GameListener listener : listeners)
				if (listener != null && listener instanceof BoardGameListener)
					try {
						((BoardGameListener) listener).onSingleMove(src, dst);
					} catch (Throwable e) {
						e.printStackTrace();
					}

			doSingleMove(srcRow, srcCol, dstRow, dstCol, i < move.count() - 2);
		}
	}

	public boolean doRandomMove() {
		if (moveList.size() == 0)
			return false;

		return doMove(RandomUtil.randomList(moveList), true, false);
	}

	protected abstract void doSingleMove(int srcRow, int srcCol, int dstRow, int dstCol, boolean hasMore);

	protected final void generateMoveList() {
		if (drawRequest)
			return;

		moveList.clear();
		generateMoveListImpl();
	}

	protected void generateMoveListImpl() {
		for (int row = 0; row < getRowCount(); row++)
			for (int col = 0; col < getColCount(); col++) {
				if (!isValidPos(row, col))
					continue;

				BoardPiece piece = getBoardInternal(row, col);
				if (piece == null)
					continue;

				if (piece.getPlayerIndex() == getCurrentTurn())
					piece.generateMoveList(moveList);
			}
	}

	public final BoardPiece getBoard(BoardPosition position) {
		return getBoard(position.getRow(), position.getCol());
	}

	public BoardPiece getBoard(int row, int col) {
		if (!isValidPos(row, col))
			throw new RuntimeException("The position is not valid: " + row + " " + col);

		return getBoardInternal(row, col);
	}

	protected BoardPiece getBoardInternal(BoardPosition position) {
		return getBoardInternal(position.getRow(), position.getCol());
	}

	protected BoardPiece getBoardInternal(int row, int col) {
		return board[col][row];
	}

	public abstract int getColCount();

	public BoardMove getLastMove() {
		if (moveHistory.size() == 0)
			return null;

		return moveHistory.get(moveHistory.size() - 1);
	}

	public BoardPiece getPieceToMove(BoardMove move) {
		BoardPosition src = move.source();
		int row = src.getRow();
		int col = src.getCol();

		return getBoard(row, col);
	}

	public abstract int getRowCount();

	public int getState() {
		return state;
	}

	public boolean hasAutoDraw() {
		return autoDraw;
	}

	public boolean hasMoves() {
		return moveList.size() > 0;
	}

	public final boolean isPaused() {
		return state == PAUSED;
	}

	private boolean isValidFirstMoves(BoardMove move) {
		for (BoardMove move1 : moveList)
			if (move1.startWith(move))
				return true;

		return false;
	}

	public final boolean isValidMove(BoardMove move) {
		if (moveList.contains(move))
			return true;

		return isValidFirstMoves(move);
	}

	public boolean isValidPos(BoardPosition position) {
		return isValidPos(position.getRow(), position.getCol());
	}

	public boolean isValidPos(int row, int col) {
		return row >= 0 && row < getRowCount() && col >= 0 && col < getColCount();
	}

	public List<BoardMove> moveHistory() {
		return Collections.unmodifiableList(moveHistory);
	}

	public int moveHistorySize() {
		return moveHistory.size();
	}

	public List<BoardMove> moveList() {
		return Collections.unmodifiableList(moveList);
	}

	public int moveListSize() {
		return moveList.size();
	}

	public void offerDraw() {
		drawRequest = true;
		nextTurn();
	}

	private synchronized void onPauseTimeOut() {
		if (!isRunning() || !isPaused())
			return;

		for (GameListener listener : listeners)
			if (listeners instanceof BoardGameListener)
				try {
					((BoardGameListener) listener).onPauseTimeOut();
				} catch (Throwable e) {
					e.printStackTrace();
				}

		resume();
	}

	@Override
	protected void onResetTurn() {
		moveHistory.clear();
		generateMoveList();
	}

	public void pause() {
		pause(DEFAULT_PAUSE_TIME);
	}

	public synchronized void pause(int time) {
		state = PAUSED;

		pauseAllTimers();

		tmrPause.setInterval(time * 1000);
		tmrPause.reset();
		tmrPause.play();

		for (GameListener listener : listeners)
			if (listeners instanceof BoardGameListener)
				try {
					((BoardGameListener) listener).onPause(time);
				} catch (Throwable e) {
					e.printStackTrace();
				}
	}

	@Override
	protected void processRound(int round) {
		super.processRound(round);
	}

	protected void rebuildBoard() {
		setupBoard();
		for (BoardMove move : moveHistory)
			doMoveInternal(move);
	}

	public void rejectDraw() {
		drawRequest = false;
		nextTurn();
	}

	public synchronized void resume() {
		if (!isRunning() || !isPaused())
			return;

		tmrPause.pause();

		state = RUNNING;

		resumeAllTimers();

		for (GameListener listener : listeners)
			if (listeners instanceof BoardGameListener)
				try {
					((BoardGameListener) listener).onResume();
				} catch (Throwable e) {
					e.printStackTrace();
				}
	}

	public void setAutoDraw(boolean autoDraw) {
		this.autoDraw = autoDraw;
	}

	public void setBoard(BoardPosition position, BoardPiece piece) {
		if (position != null && !isValidPos(position))
			throw new RuntimeException("The position is not valid: " + position);

		setBoardInternal(position, piece);
	}

	public final void setBoard(int row, int col, BoardPiece piece) {
		setBoard(new BoardPosition(row, col), piece);
		
		for (GameListener listener: listeners)
			if (listener != null && listener instanceof BoardGameListener)
				((BoardGameListener) listener).onSetBoard(row, col, piece);
	}

	protected void setBoardInternal(BoardPosition position, BoardPiece piece) {
		if (piece != null && piece.position != null) {
			int srcRow = piece.position.getRow();
			int srcCol = piece.position.getCol();
			board[srcCol][srcRow] = null;
		}
		if (position != null) {
			int row = position.getRow();
			int col = position.getCol();
			if (board[col][row] != null)
				board[col][row].position = null;
			board[col][row] = piece;
		}
		if (piece != null)
			piece.position = position;
	}

	protected void setBoardInternal(int row, int col, BoardPiece piece) {
		setBoardInternal(new BoardPosition(row, col), piece);
	}

	protected abstract void setupBoard();

	@Override
	public void stop(StopReason reason) {
		if (!isRunning())
			return;

		tmrPause.pause();

		state = STOPED;

		super.stop(reason);
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		int rowCount = getRowCount();
		int colCount = getColCount();

		pw.println("Board:");
		pw.print(" ");
		for (int col = 0; col < colCount; col++)
			pw.print(" " + (colCount - col - 1));
		pw.println();
		for (int row = rowCount - 1; row >= 0; row--) {
			pw.print(rowCount - row - 1);
			for (int col = 0; col < colCount; col++) {
				pw.print(" ");
				if (!isValidPos(rowCount - row - 1, colCount - col - 1))
					pw.print(" ");
				else {
					BoardPiece piece = getBoardInternal(rowCount - row - 1, colCount - col - 1);
					pw.print(piece != null ? piece.toString() : " ");
				}
			}
			pw.println();
		}

		pw.println("Turn: " + turnToStr(getCurrentTurn()));
		pw.println("Move list: " + moveList);
		pw.println("Move history: " + moveHistory);

		return sw.toString();
	}

	protected abstract String turnToStr(int currentTurn);

	public synchronized boolean undoLastMove() {
		if (!isRunning())
			return false;
		if (moveHistory.size() == 0)
			return false;

		moveHistory.remove(moveHistory.size() - 1);
		rebuildBoard();
		previousTurn();

		for (GameListener listener : listeners)
			if (listeners instanceof BoardGameListener)
				try {
					((BoardGameListener) listener).onUndoLastMove();
				} catch (Throwable e) {
					e.printStackTrace();
				}

		return true;
	}

}
