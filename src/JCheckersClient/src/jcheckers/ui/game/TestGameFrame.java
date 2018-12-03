package jcheckers.ui.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import common.process.ProcessQueue;
import jcheckers.common.logic.Game.StopReason;
import jcheckers.common.logic.Player;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPosition;
import jcheckers.common.logic.boards.draughts.DraughtsConfig;
import jcheckers.common.logic.boards.draughts.DraughtsGame;
import jcheckers.common.logic.boards.draughts.DraughtsGameListener;
import jcheckers.common.logic.boards.draughts.DraughtsMan;
import jcheckers.common.logic.boards.draughts.DraughtsPiece;
import jcheckers.ui.ImageList;
import jcheckers.ui.ImageListImpl;
import jcheckers.ui.board.GameBoard;

public class TestGameFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2570472551275516539L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				TestGameFrame frame = new TestGameFrame();
				frame.setVisible(true);
				frame.setSize(new Dimension(512, 512));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private ImageList imgList;
	private DraughtsGame game;
	private ProcessQueue queue;
	private GameBoard board;

	/**
	 * Create the frame.
	 */
	public TestGameFrame() {
		queue = new ProcessQueue();

		imgList = new ImageListImpl("images");
		imgList.init();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				board.close();
				imgList.destroy();
				game.close();
				queue.close();
			}
		});

		setSize(new Dimension(511, 511));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		game = new DraughtsGame(() -> queue);

		game.addListener(new DraughtsGameListener() {

			@Override
			public void onCapture(DraughtsPiece piece, int row, int col) {

			}

			@Override
			public void onChangeTurn(int turn) {
				System.out.println("Change turn: " + turn);
			}

			@Override
			public void onMove(BoardMove move) {

			}

			@Override
			public void onNextRound(int round) {

			}

			@Override
			public void onPause(int time) {
				System.out.println("Game paused for " + time + "ms");
			}

			@Override
			public void onPauseTimeOut() {
				System.out.println("Game pause timeout!");
			}

			@Override
			public void onPromote(DraughtsMan man, int row, int col) {

			}

			@Override
			public void onResume() {
				System.out.println("Game resumed!");
			}

			@Override
			public void onRotateLeft() {

			}

			@Override
			public void onRotateRight() {

			}

			@Override
			public void onSingleMove(BoardPosition src, BoardPosition dst) {

			}

			@Override
			public void onStart() {
				System.out.println("Game starting...");
			}

			@Override
			public void onStarted() {
				System.out.println("Game started!");
			}

			@Override
			public void onStop(StopReason reason) {
				System.out.println("Game stoped: " + reason);
			}

			@Override
			public void onSwap(int index1, int index2) {

			}

			@Override
			public void onUndoLastMove() {
				System.out.println("Undo last move!");
			}
		});

		game.setConfig(DraughtsConfig.AMERICAN);
		game.setUseTime(false);
		game.setUseIncrementTime(false);
		game.setUseTimePerTurn(false);
		Player white = game.join(DraughtsGame.WHITE, "White");
		Player black = game.join(DraughtsGame.BLACK, "Black");

		white.start();
		black.start();
		if (!game.start()) {
			System.err.println("Game not started!");
			return;
		}

		board = new GameBoard(imgList, game);
		getContentPane().add(board, BorderLayout.CENTER);
	}

}
