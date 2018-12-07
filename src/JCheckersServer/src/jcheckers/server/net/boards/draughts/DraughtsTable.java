package jcheckers.server.net.boards.draughts;

import jcheckers.common.logic.MultiplayerGame;
import jcheckers.common.logic.Player;
import jcheckers.common.logic.boards.BoardPiece;
import jcheckers.common.logic.boards.draughts.DraughtsConfig;
import jcheckers.common.logic.boards.draughts.DraughtsGame;
import jcheckers.common.logic.boards.draughts.DraughtsGameController;
import jcheckers.common.logic.boards.draughts.DraughtsGameListener;
import jcheckers.common.logic.boards.draughts.DraughtsMan;
import jcheckers.common.logic.boards.draughts.DraughtsPiece;
import jcheckers.common.logic.boards.draughts.DraughtsPlayer;
import jcheckers.server.net.User;
import jcheckers.server.net.boards.BoardTable;
import jcheckers.server.net.boards.BoardUser;

public class DraughtsTable extends BoardTable {

	protected class DraughtsController extends BoardController implements DraughtsGameController {

	}

	protected class DraughtsListener extends BoardListener implements DraughtsGameListener {

		@Override
		public void onCapture(DraughtsPiece piece, int row, int col) {

		}

		@Override
		public void onPromote(DraughtsMan man, int row, int col) {

		}

		@Override
		public void onSetBoard(int row, int col, BoardPiece piece) {

		}

		@Override
		public void onChangeConfig(DraughtsConfig config) {

		}

	}

	public class DraughtsSeat extends BoardSeat {

		protected DraughtsSeat(int index, BoardUser user) {
			super(index, user);
		}

	}

	private static final int DRAUGHTS = 14;

	public static final int CLASSIC = 0;
	public static final int INTERNATIONAL = 1;
	public static final int AMERICAN = 2;
	public static final int AMERICAN10x10 = 3;

	private DraughtsGame game;

	public DraughtsTable() {
		super();
	}

	@Override
	protected void afterClose() {
		super.afterClose();

		game = null;
	}

	@Override
	protected MultiplayerGame createGame() {
		game = new DraughtsGame(new DraughtsController());
		game.addListener(new DraughtsListener());
		return game;
	}

	@Override
	protected Player[] createPlayers() {
		return new DraughtsPlayer[2];
	}

	@Override
	protected Seat createSeat(int seatIndex, User user) {
		return new DraughtsSeat(seatIndex, (DraughtsUser) user);
	}

	@Override
	protected Seat[] createSeats() {
		return new DraughtsSeat[2];
	}

	@Override
	protected DraughtsGame getGame() {
		return game;
	}

	@Override
	protected int getGameID() {
		return DRAUGHTS;
	}

	@Override
	public int getMaxSeatCount() {
		return 2;
	}

	@Override
	public DraughtsServer getServer() {
		return (DraughtsServer) super.getServer();
	}

	@Override
	protected void notifyGameState(User user) {
		notifyGameState1(user);

		super.notifyGameState(user);
	}

	@Override
	protected void updateGameType() {
		int gameType = getGameType();
		switch (gameType) {
			case CLASSIC:
				game.setConfig(DraughtsConfig.BRAZILIAN);
				return;
			case AMERICAN:
				game.setConfig(DraughtsConfig.AMERICAN);
				return;
			case INTERNATIONAL:
				game.setConfig(DraughtsConfig.INTERNATIONAL);
				return;
			case AMERICAN10x10:
				game.setConfig(DraughtsConfig.AMERICAN10x10);
				return;
			default:
				throw new RuntimeException("Invalid game type " + gameType);
		}
	}

	@Override
	protected boolean validateGameType(int gameType) {
		return gameType == CLASSIC || gameType == AMERICAN || gameType == INTERNATIONAL || gameType == AMERICAN10x10;
	}

}
