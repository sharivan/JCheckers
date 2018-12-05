package jcheckers.ui.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import common.util.Tree;
import common.util.Tree.Node;
import jcheckers.common.logic.Game.StopReason;
import jcheckers.common.logic.MultiplayerGame;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPiece;
import jcheckers.common.logic.boards.BoardPosition;
import jcheckers.common.logic.boards.draughts.DraughtsGame;
import jcheckers.common.logic.boards.draughts.DraughtsGameListener;
import jcheckers.common.logic.boards.draughts.DraughtsKing;
import jcheckers.common.logic.boards.draughts.DraughtsMan;
import jcheckers.common.logic.boards.draughts.DraughtsPiece;
import jcheckers.ui.ImageList;

public class GameBoard extends JPanel {

	public class Piece {

		private BoardPosition position;
		private int x;
		private int y;
		private boolean dragging;
		private Tree<BoardPosition> moveTree;

		private boolean king = false;
		private PieceColor color = PieceColor.NONE;

		private Piece(int row, int col, PieceColor color) {
			this(row, col, color, false);
		}

		private Piece(int row, int col, PieceColor color, boolean king) {
			position = new BoardPosition(row, col);
			this.color = color;
			this.king = king;
		}

		private void drawHighlightPosition(Graphics2D g2D, Node<BoardPosition> node, int size) {
			BoardPosition position = translatePosition(node.getValue());
			int highlightX = position.getCol() * size;
			int highlightY = position.getRow() * size;

			g2D.setStroke(HIGHLIGHT_STROKE);
			g2D.setColor(HIGHLIGHT_COLOR);
			g2D.drawRect(highlightX, highlightY, size - 2, size - 2);

			for (int i = 0; i < node.getChildCount(); i++) {
				Node<BoardPosition> child = node.getChild(i);
				drawHighlightPosition(g2D, child, size);
			}
		}

		public int getCol() {
			return position.getCol();
		}

		public PieceColor getColor() {
			return color;
		}

		public BoardPosition getPosition() {
			return position;
		}

		public int getRow() {
			return position.getRow();
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public boolean isDragging() {
			return dragging;
		}

		public boolean isKing() {
			return king;
		}

		protected void paintComponent(Graphics2D g2D, int size) {
			if (!dragging) {
				BoardPosition position = translatePosition(this.position);
				x = position.getCol() * size;
				y = position.getRow() * size;
			}

			if (moveTree != null)
				drawHighlightPosition(g2D, moveTree.getRoot(), size);

			Image img = null;
			switch (color) {
				case BLACK:
					img = king ? bkImg : bmImg;
					break;

				case WHITE:
					img = king ? wkImg : wmImg;
					break;

				default:
					break;
			}

			if (img != null)
				g2D.drawImage(img, x, y, size, size, null);
		}

		public void setColor(PieceColor color) {
			this.color = color;
			repaint();
		}

		public void setKing(boolean value) {
			king = value;
			repaint();
		}

		public void setPosition(BoardPosition position) {
			this.position = position;
			repaint();
		}

		public void setPosition(int row, int col) {
			this.setPosition(new BoardPosition(row, col));
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8358675169395868301L;

	private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 0);
	private static final Color HIGHLIGHT_COLOR = Color.YELLOW;
	private static final Stroke HIGHLIGHT_STROKE = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final int DEFAULT_SIZE = 200;

	private static Tree<BoardPosition> buildMoveTree(List<BoardMove> moveList) {
		if (moveList == null || moveList.size() == 0)
			return null;

		Tree<BoardPosition> result = new Tree<>(moveList.get(0).get(0));
		Node<BoardPosition> root = result.getRoot();
		for (BoardMove move : moveList)
			buildMoveTree(root, move, 1);

		return result;
	}

	private static void buildMoveTree(Node<BoardPosition> parent, BoardMove move, int index) {
		if (index >= move.count())
			return;

		BoardPosition firstPostion = move.get(index);
		parent.addChild(firstPostion);

		buildMoveTree(parent, move, index + 1);
	}

	private DraughtsGame game;

	private Image darkImg;
	private Image lightImg;
	private Image bmImg;
	private Image wmImg;

	private Image bkImg;
	private Image wkImg;

	private ArrayList<Piece> pieces;
	private boolean locked = true;
	private boolean rotated = false;
	private int lastX = -1;
	private int lastY = -1;
	private Piece draggingPiece;
	private BoardPosition srcPosition;
	private Piece highlightPiece;

	private DraughtsGameListener gameHandler;
	private BoardPosition promotePosition;

	private boolean showPossibleMoves;

	private boolean highlightLastMove;

	public GameBoard(ImageList imgList, DraughtsGame game) {
		pieces = new ArrayList<>();

		if (imgList != null) {
			darkImg = imgList.getImage("dark.png");
			lightImg = imgList.getImage("light.png");

			bmImg = imgList.getImage("bm.png");
			wmImg = imgList.getImage("wm.png");
			bkImg = imgList.getImage("bk.png");
			wkImg = imgList.getImage("wk.png");
		}

		setBorder(new LineBorder(new Color(0, 0, 0)));
		setMinimumSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
		setLayout(null);

		gameHandler = new DraughtsGameListener() {

			@Override
			public void onCapture(DraughtsPiece piece, int row, int col) {
				Piece myPiece = getPieceAt(row, col);
				pieces.remove(myPiece);
				repaint();
			}

			@Override
			public void onChangeTurn(int turn) {

			}

			@Override
			public void onMove(BoardMove move) {

			}

			@Override
			public void onNextRound(int round) {

			}

			@Override
			public void onPause(int time) {

			}

			@Override
			public void onPauseTimeOut() {

			}

			@Override
			public void onPromote(DraughtsMan man, int row, int col) {
				promotePosition = new BoardPosition(row, col);
			}

			@Override
			public void onResume() {

			}

			@Override
			public void onRotateLeft() {

			}

			@Override
			public void onRotateRight() {

			}

			@Override
			public void onSingleMove(BoardPosition src, BoardPosition dst) {
				Piece myPiece = getPieceAt(src);
				if (myPiece != null) {
					myPiece.setPosition(dst);
					repaint();
				}
			}

			@Override
			public void onStart() {

			}

			@Override
			public void onStarted() {
				refreshInternal();
			}

			@Override
			public void onStop(StopReason reason) {
				setLocked(true);
			}

			@Override
			public void onSwap(int index1, int index2) {

			}

			@Override
			public void onUndoLastMove() {

			}
		};

		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				onMousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				onMouseReleased(e);
			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				onMouseDragged(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				onMouseMoved(e);
			}
		});

		setGame(game);

		repaint();
	}

	public Piece addPiece(int row, int col, PieceColor color, boolean king) {
		Piece piece = new Piece(row, col, color, king);
		pieces.add(piece);
		return piece;
	}

	public void close() {
		setGame(null);
	}

	private void drawSquare(Graphics2D g2D, int x, int y, int size, SquareColor color) {
		Image img = null;
		switch (color) {
			case DARK:
				img = darkImg;
				break;

			case LIGHT:
				img = lightImg;
				break;
		}

		g2D.drawImage(img, x, y, size, size, null);
	}

	public int getBlackCount() {
		int result = 0;
		for (Piece piece : pieces)
			if (piece.getColor() == PieceColor.BLACK)
				result++;

		return result;
	}

	public int getBlackKingCount() {
		int result = 0;
		for (Piece piece : pieces)
			if (piece.getColor() == PieceColor.BLACK && piece.isKing())
				result++;

		return result;
	}

	public int getBlackManCount() {
		int result = 0;
		for (Piece piece : pieces)
			if (piece.getColor() == PieceColor.BLACK && !piece.isKing())
				result++;

		return result;
	}

	public int getColCount() {
		return game != null ? game.getColCount() : 8;
	}

	public DraughtsGame getGame() {
		return game;
	}

	public int getKingCount() {
		int result = 0;
		for (Piece piece : pieces)
			if (piece.isKing())
				result++;

		return result;
	}

	public int getManCount() {
		int result = 0;
		for (Piece piece : pieces)
			if (!piece.isKing())
				result++;

		return result;
	}

	public Piece getPiece(int index) {
		return pieces.get(index);
	}

	public Piece getPieceAt(BoardPosition pos) {
		return getPieceAt(pos.getRow(), pos.getCol());
	}

	public Piece getPieceAt(int row, int col) {
		for (Piece piece : pieces)
			if (piece.getRow() == row && piece.getCol() == col)
				return piece;

		return null;
	}

	public Piece getPieceAtCoords(int x, int y) {
		int squareWidth = getSquareSize();
		int squareHeight = getSquareSize();
		for (Piece piece : pieces) {
			int px = piece.getX();
			int py = piece.getY();
			if (px <= x && x < px + squareWidth && py <= y && y < py + squareHeight)
				return piece;
		}

		return null;
	}

	public int getPieceCount() {
		return pieces.size();
	}

	public int getRowCount() {
		return game != null ? game.getRowCount() : 8;
	}

	private SquareColor getSquareColor(BoardPosition position) {
		return (game != null ? game.isValidPos(position.getRow(), position.getCol()) : (position.getRow() + position.getCol() & 1) == 0) ? SquareColor.DARK : SquareColor.LIGHT;
	}

	public int getSquareSize() {
		int sqw = getWidth() / getColCount();
		int sqh = getHeight() / getRowCount();
		return Math.min(sqw, sqh);
	}

	public PieceColor getTurn() {
		if (game == null)
			return PieceColor.NONE;

		int turn = game.getCurrentTurn();
		return playerIndexToPieceColor(turn);
	}

	public int getWhiteCount() {
		int result = 0;
		for (Piece piece : pieces)
			if (piece.getColor() == PieceColor.WHITE)
				result++;

		return result;
	}

	public int getWhiteKingCount() {
		int result = 0;
		for (Piece piece : pieces)
			if (piece.getColor() == PieceColor.WHITE && piece.isKing())
				result++;

		return result;
	}

	public int getWhitekManCount() {
		int result = 0;
		for (Piece piece : pieces)
			if (piece.getColor() == PieceColor.WHITE && !piece.isKing())
				result++;

		return result;
	}

	public boolean isHighlightLastMove() {
		return highlightLastMove;
	}

	public boolean isLocked() {
		return locked;
	}

	public boolean isRotated() {
		return rotated;
	}

	public boolean isShowPossibleMoves() {
		return showPossibleMoves;
	}

	public void onMouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		if (draggingPiece != null) {
			int dx = x - lastX;
			int dy = y - lastY;
			draggingPiece.x += dx;
			draggingPiece.y += dy;
			repaint();
		}

		lastX = x;
		lastY = y;
	}

	private void onMouseMoved(MouseEvent e) {
		if (locked || game == null || !game.isRunning())
			return;

		int x = e.getX();
		int y = e.getY();

		Piece piece = getPieceAtCoords(x, y);
		List<BoardMove> moveList = null;
		if (piece != null)
			if (playerIndexToPieceColor(game.getCurrentTurn()) != piece.getColor())
				piece = null;
			else {
				BoardPiece boardPiece = game.getBoard(piece.getPosition());
				moveList = boardPiece.getMoveList();
				if (moveList.size() == 0)
					piece = null;
			}
		if (piece == null && highlightPiece != null || piece != null && !piece.equals(highlightPiece)) {
			if (highlightPiece != null)
				highlightPiece.moveTree = null;

			highlightPiece = piece;

			if (highlightPiece != null)
				highlightPiece.moveTree = buildMoveTree(moveList);

			repaint();
		}

		lastX = x;
		lastY = y;
	}

	private void onMousePressed(MouseEvent e) {
		if (locked || game == null || !game.isRunning())
			return;

		int x = e.getX();
		int y = e.getY();
		Piece piece = getPieceAtCoords(x, y);
		if (piece == null)
			return;

		if (playerIndexToPieceColor(game.getCurrentTurn()) != piece.getColor())
			return;

		BoardPosition position = piece.getPosition();
		BoardPiece boardPiece = game.getBoard(position);
		List<BoardMove> moveList = boardPiece.getMoveList();
		if (moveList.size() == 0)
			return;

		srcPosition = position;
		draggingPiece = piece;
		piece.dragging = true;
	}

	private void onMouseReleased(MouseEvent e) {
		if (locked || game == null || draggingPiece == null)
			return;

		draggingPiece.dragging = false;

		int x = e.getX();
		int y = e.getY();
		int col = x * getColCount() / getHeight();
		int row = y * getRowCount() / getWidth();
		BoardPosition dstPosition = translatePosition(row, col);

		BoardMove move = new BoardMove(srcPosition, dstPosition);
		if (game.doMove(move, true))
			draggingPiece.setPosition(dstPosition);
		else
			draggingPiece.setPosition(srcPosition);

		if (draggingPiece.getPosition().equals(promotePosition)) {
			draggingPiece.setKing(true);
			promotePosition = null;
		}

		draggingPiece = null;
		srcPosition = null;

		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2D = (Graphics2D) g;
		g2D.setBackground(BACKGROUND_COLOR);
		g2D.clearRect(0, 0, getWidth(), getHeight());
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Habilita
																									// Antialising

		int rowCount = getRowCount();
		int colCount = getColCount();
		int squareSize = getSquareSize();
		for (int col = 0; col < colCount; col++)
			for (int row = 0; row < rowCount; row++)
				drawSquare(g2D, col * squareSize, row * squareSize, squareSize, getSquareColor(translatePosition(row, col)));

		for (Piece piece : pieces)
			piece.paintComponent(g2D, squareSize);
	}

	private PieceColor playerIndexToPieceColor(int playerIndex) {
		switch (playerIndex) {
			case MultiplayerGame.NO_TURN:
				return PieceColor.NONE;

			case DraughtsGame.WHITE:
				return PieceColor.WHITE;

			case DraughtsGame.BLACK:
				return PieceColor.BLACK;
		}

		return PieceColor.NONE;
	}

	public void refresh() {
		refreshInternal();
		revalidate();
		repaint();
	}

	private void refreshInternal() {
		pieces.clear();

		if (game == null)
			return;

		int rowCount = getRowCount();
		int colCount = getColCount();
		for (int row = 0; row < rowCount; row++)
			for (int col = 0; col < colCount; col++) {
				if (!game.isValidPos(row, col))
					continue;

				DraughtsPiece piece = (DraughtsPiece) game.getBoard(row, col);
				if (piece != null)
					addPiece(row, col, playerIndexToPieceColor(piece.getPlayerIndex()), piece instanceof DraughtsKing);
			}
	}

	public void setGame(DraughtsGame game) {
		if (this.game != null)
			this.game.removeListener(gameHandler);

		this.game = game;

		if (game != null)
			game.addListener(gameHandler);

		refreshInternal();
	}

	public void setHighlightLastMove(boolean highlightLastMove) {
		this.highlightLastMove = highlightLastMove;
		repaint();
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		repaint();
	}

	public void setRotated(boolean value) {
		rotated = value;
		repaint();
	}

	public void setShowPossibleMoves(boolean showPossibleMoves) {
		this.showPossibleMoves = showPossibleMoves;
	}

	private BoardPosition translatePosition(BoardPosition position) {
		return new BoardPosition(rotated ? position.getRow() : getRowCount() - position.getRow() - 1, rotated ? getColCount() - position.getCol() - 1 : position.getCol());
	}

	private BoardPosition translatePosition(int row, int col) {
		return new BoardPosition(rotated ? row : getRowCount() - row - 1, rotated ? getColCount() - col - 1 : col);
	}

}
