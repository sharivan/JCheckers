package jcheckers.server.net.boards;

import java.io.IOException;
import java.util.List;

import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.logic.Game.StopReason;
import jcheckers.common.logic.MultiplayerGame;
import jcheckers.common.logic.Player;
import jcheckers.common.logic.boards.BoardGame;
import jcheckers.common.logic.boards.BoardGameController;
import jcheckers.common.logic.boards.BoardGameListener;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPiece;
import jcheckers.common.logic.boards.BoardPlayer;
import jcheckers.common.logic.boards.BoardPosition;
import jcheckers.common.logic.boards.RatingSystem;
import jcheckers.server.io.OutputProtocol;
import jcheckers.server.io.boards.BoardInputProtocol;
import jcheckers.server.io.boards.BoardOutputProtocol;
import jcheckers.server.net.Room;
import jcheckers.server.net.Server;
import jcheckers.server.net.Table;
import jcheckers.server.net.User;

public abstract class BoardTable extends Table {

	protected class BoardController extends Controller implements BoardGameController {

		public BoardController() {
			super();
		}

	}

	protected class BoardListener extends Listener implements BoardGameListener {
		@Override
		public void onChangeTurn(int turn) {
			super.onChangeTurn(turn);

			BoardGame game = getGame();
			if (DEBUG)
				logToOut(System.lineSeparator() + game.toString());
		}

		@Override
		public void onMove(BoardMove move) {

		}

		@Override
		public void onPause(int time) {
			if (DEBUG)
				logToOut("Game paused.");

			notifyGameStateAll();
		}

		@Override
		public void onPauseTimeOut() {
			if (DEBUG)
				logToOut("Game resumed after pause timeout.");

			if (whosOfferingDraw != null) {
				User user = null;
				for (Seat seat : seats) {
					if (seat == null || seat.equals(whosOfferingDraw))
						continue;

					User user1 = seat.getUser();
					if (user1 != null) {
						user = user1;
						break;
					}
				}

				BoardUser other = (BoardUser) whosOfferingDraw.getUser();
				if (user != null && other != null) {
					BoardOutputProtocol protocol = other.prepareProtocol();
					protocol.notifyPlayerRejectedDraw(user.getName(), user.getID());
				}

				whosOfferingDraw = null;
			}

			if (whosSuggestedUndoMove != null) {
				User user = null;
				for (Seat seat : seats) {
					if (seat == null || seat.equals(whosSuggestedUndoMove))
						continue;

					User user1 = seat.getUser();
					if (user1 != null) {
						user = user1;
						break;
					}
				}

				BoardUser other = (BoardUser) whosSuggestedUndoMove.getUser();
				if (user != null && other != null) {
					BoardOutputProtocol protocol = other.prepareProtocol();
					protocol.notifyPlayerRejectedUndoMove(user.getName(), user.getID());
				}

				whosSuggestedUndoMove = null;
			}
		}

		@Override
		public void onResume() {
			if (DEBUG)
				logToOut("Game resumed.");

			whosOfferingDraw = null;
			whosSuggestedUndoMove = null;

			notifyGameStateAll();
		}

		@Override
		public void onSingleMove(BoardPosition src, BoardPosition dst) {

		}

		@Override
		public void onStart() {
			BoardOutputProtocol protocol = (BoardOutputProtocol) prepareStartedBroadCastProtocol();
			if (protocol != null)
				protocol.hideControls();

			if (swapSides)
				getGame().rotateLeft();

			super.onStart();
		}

		@Override
		public void onUndoLastMove() {
			if (DEBUG)
				logToOut("Game undo last move.");
		}

		@Override
		public void onSetBoard(int row, int col, BoardPiece piece) {

		}
	}

	public abstract class BoardSeat extends Seat {

		protected BoardSeat(int index, BoardUser user) {
			super(index, user);
		}

		@Override
		public BoardPlayer getPlayer() {
			return (BoardPlayer) super.getPlayer();
		}

		public int getRating() {
			BoardUser user = getUser();

			user.fetchData();

			return user.getRating();
		}

		@Override
		public BoardUser getUser() {
			return (BoardUser) super.getUser();
		}

	}

	protected class PauseGameQuestion extends SeatQuestion {

		private int time;

		public PauseGameQuestion() {
			super();
		}

		public int getTime() {
			return time;
		}

		public void open(int time, Seat questioner, List<Seat> questioneds, QuestionTimeout<Seat, Seat> callback) {
			super.open(questioner, questioneds, RESPONSE_INTERVAL, callback);

			this.time = time;
		}

		@Override
		public void open(Seat questioner, List<Seat> questioneds, QuestionTimeout<Seat, Seat> callback) {
			open(10, questioner, questioneds, callback);
		}

	}

	private static final boolean DEBUG = true;

	public static final int RATING_MARGIN = 200;

	public static final int CHALLANGE = 2;

	private int gameType;
	private boolean rated = true;
	private boolean hasRatingLimit = false;
	private int minRating;
	private int maxRating;
	private boolean activeHelp = true;
	private boolean swapSides = true;

	private Seat whosOfferingDraw;
	private Seat whosSuggestedUndoMove;

	protected PauseGameQuestion pauseGameQuestion;

	private RatingSystem<Seat> system;

	protected BoardTable() {
		super();
	}

	protected void acceptPauseGame(Seat seat) {
		if (pauseGameQuestion == null)
			return;

		synchronized (pauseGameQuestion) {
			if (!pauseGameQuestion.isOpen() || !pauseGameQuestion.accept(seat))
				return;

			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.questionBits(pauseGameQuestion.getAcceptedBits(), 0);

			if (pauseGameQuestion.allAccepted()) {
				pauseGameQuestion.close();

				// TODO veriricar se é mesmo assim que deve ser feito isso
				getGame().pause(pauseGameQuestion.getTime());
			}
		}
	}

	@Override
	protected void afterClose() {
		super.afterClose();

		pauseGameQuestion = null;
		whosOfferingDraw = null;
		whosSuggestedUndoMove = null;
	}

	@Override
	protected void beforeClose() {
		pauseGameQuestion.terminate();

		super.beforeClose();
	}

	@Override
	protected void defaultConfig() {
		super.defaultConfig();
	}

	@Override
	protected BoardGame getGame() {
		return (BoardGame) super.getGame();
	}

	public int getGameType() {
		return gameType;
	}

	@Override
	public BoardUser getHost() {
		return (BoardUser) super.getHost();
	}

	@Override
	public BoardSeat getSeat(User user) {
		return (BoardSeat) super.getSeat(user);
	}

	@Override
	public BoardServer getServer() {
		return (BoardServer) super.getServer();
	}

	public boolean isActiveHelp() {
		return activeHelp;
	}

	public boolean isRated() {
		return rated;
	}

	public boolean isSwapSides() {
		return swapSides;
	}

	@Override
	protected void notifyGameState(User user) {
		BoardGame game = getGame();
		JCheckersDataOutputStream output = user.prepareOutput();
		if (output != null) {
			List<BoardMove> moveHistory = game.moveHistory();
			int moveCount = moveHistory.size();

			int currentTurn = game.getCurrentTurn();

			OutputProtocol protocol = user.prepareProtocol();
			if (protocol == null)
				return;
			
			output.writeUChar(protocol.getOpcode(BoardOutputProtocol.DRAUGHTS_AND_CHESS_GAME_STATE));
			output.writeChar(getGameID()); // game id
			output.writeChar(getGameType()); // game type (0=classic
												// 1=international 2=american
												// 3=american 10x10)
			output.writeBoolean(game.isRunning()); // running
			output.writeChar(game.getState()); // state (0=created 1=running
												// 3=stoped 5=paused for
												// questioning)
			output.writeChar(currentTurn != MultiplayerGame.NO_TURN ? game.getCurrentTurn() : 4); // current
																									// turn
																									// (4=no
																									// turn)
			output.writeChar(game.getState() == BoardGame.CREATED ? 0 : 3); // 3=has
																			// moves
																			// other=initialstate
																			// 0=crash
			output.writeChar(game.getState() == BoardGame.CREATED ? 0 : game.getState() == BoardGame.RUNNING ? 3 : 1); // time
																														// flags
			output.writeChar(game.getState() == BoardGame.CREATED ? 1 : 0); // ?
			output.writeInt(game.getTimePerTurn()); // ?
			output.writeInt(game.getTimePerTurn()); // time per move
			output.writeBoolean(false);
			output.writeInt(game.getState() == BoardGame.CREATED ? 0 : 1);
			output.writeBoolean(game.getState() != BoardGame.CREATED);
			output.writeInt(game.getState() == BoardGame.CREATED ? 0 : 1);
			output.writeInt(game.getState() == BoardGame.CREATED ? 1557409520 : 0);
			output.writeChar(3);
			output.writeInt(0);
			
			List<Player> players = game.players();
			if (game.getState() != BoardGame.CREATED) {
				output.writeChar(players.size());
				for (int i = 0; i < players.size(); i++) {
					Player player = players.get(i);
					if (player != null && player.isPlaying())
						output.writeInt(player.getCurrentTime());
					else
						output.writeInt(0);
				}
			} else
				output.writeChar(0);
			
			output.writeChar(0);
			output.writeInt(0);

			output.writeInt(2);
			for (@SuppressWarnings("unused")
			Player player : players)
				// if (player != null)
				// output.writeString(player.getName());
				// else
				output.writeShort(0);

			output.writeChar(0); // aqui tem um loop com base no valor escrito
									// aqui
			output.writeInt(moveCount);
			int turn = game.getInitialTurn();
			for (int turnNum = 0; turnNum < moveCount; turnNum++) {
				output.writeChar(turn);
				output.writeChar(0);
				output.writeShort(0);
				output.writeInt(0);
				turn = 1 - turn;
				BoardMove move = moveHistory.get(turnNum);
				int positions = move.count();
				output.writeChar(3 * positions - 4);
				if (positions > 1) {
					BoardPosition src = move.source();
					for (int posIndex = 1; posIndex < positions; posIndex++) {
						if (posIndex > 1) {
							output.writeInt(-1);
							output.writeInt(-1);
						}
						BoardPosition dst = move.get(posIndex);
						output.writeInt(game.getColCount() - src.getCol() - 1);
						output.writeInt(src.getRow());
						output.writeInt(game.getColCount() - dst.getCol() - 1);
						output.writeInt(dst.getRow());
						src = dst;
					}
				}
			}
			output.writeChar(0);
			output.writeChar(0);
			output.writeChar(0);
			output.writeInt(1557409520);

			output.flush();
		}
	}

	protected void notifyGameState1(User user) {

	}

	private void onPauseGameTimeOut(Question<Seat, Seat> question, List<Seat> accepteds) {
		post(() -> {
			if (pauseGameQuestion != null)
				synchronized (pauseGameQuestion) {
					if (!pauseGameQuestion.isOpen())
						return;

					pauseGameQuestion.close();

					OutputProtocol protocol = prepareBroadCastProtocol();
					if (protocol != null)
						protocol.questionCanceled();
				}
		});
	}

	@Override
	protected void onStopGame(StopReason reason) {
		if (reason != StopReason.CANCELED && rated) {
			BoardServer server = getServer();

			system.cleanup();

			int counter = 0;
			for (Seat seat : seats) {
				if (seat == null)
					continue;

				BoardUser user = (BoardUser) seat.getUser();
				if (user == null)
					continue;

				Player player = seat.getPlayer();
				if (player == null)
					continue;

				user.fetchData();

				system.addRating(seat, user.getRating());

				if (player.isWinner())
					system.setWinner(counter);

				counter++;
			}

			system.compute();

			int[] sitIndexes = new int[system.count()];
			String[] names = new String[system.count()];
			int[] gains = new int[system.count()];
			int[] ratings = new int[system.count()];
			for (int index = 0; index < system.count(); index++) {
				Seat seat = system.getPlayer(index);
				BoardUser user = (BoardUser) seat.getUser();
				Player player = seat.getPlayer();

				int rating = Math.round(system.getNewRating(index));

				sitIndexes[index] = seat.getIndex();
				names[index] = user.getName();
				gains[index] = rating - user.getRating();
				ratings[index] = rating;

				if (player.isWinner())
					server.incrementWins(user.getName(), rating);
				else if (player.isLoser())
					server.incrementLosses(user.getName(), player.isAbandoned(), rating);
				else if (player.isDraw())
					server.incrementDraws(user.getName(), rating);
				else
					throw new RuntimeException("Invalid player result " + player.getResult());

				user.fetchData();
				server.notifyUpdated(user.getName());
			}

			// TODO Fazer a chamada correta do método notifyWinners aqui

			BoardOutputProtocol protocol = (BoardOutputProtocol) prepareBroadCastProtocol();
			protocol.notifyRatingChange(sitIndexes, names, gains, ratings);
		}

		super.onStopGame(reason);
	}

	@Override
	protected void onTableRefreshParameters(JCheckersDataInputStream input) throws IOException {
		int gameID = input.readChar();
		if (gameID != getGameID())
			throw new IOException("The game id shold be " + getGameID() + " and not " + gameID);

		readConfig(input);
	}

	@Override
	protected void open(Server server, Room lobby, int number, JCheckersDataInputStream input) throws IOException {
		super.open(server, lobby, number, input);

		BoardServer boardServer = (BoardServer) server;

		minRating = boardServer.getMinRating();
		maxRating = boardServer.getMaxRating();

		whosOfferingDraw = null;
		whosSuggestedUndoMove = null;

		pauseGameQuestion = new PauseGameQuestion();

		system = new RatingSystem<>();
		system.setMinRating(boardServer.getMinRating());
		system.setMaxRating(boardServer.getMaxRating());
	}

	@Override
	protected void parseDataInternal(User user, int opcode, JCheckersDataInputStream input) throws IOException {
		BoardUser boardUser = (BoardUser) user;
		BoardUser other;
		BoardGame game = getGame();
		BoardSeat seat;
		BoardPlayer player;
		BoardOutputProtocol protocol;
		switch (opcode) {
			case BoardInputProtocol.CHECKERS_AND_CHESS_MOVE:
				if (!game.isRunning())
					return;

				seat = getSeat(boardUser);
				if (seat == null)
					return;

				player = seat.getPlayer();
				if (player == null)
					return;

				if (!player.isMyTurn())
					return;

				int turnNum = input.readInt();
				if (turnNum != game.moveHistory().size())
					return;

				int turn = input.readShort();
				if (turn != player.getIndex())
					return;

				input.readShort();
				int positionCount = input.readUChar();
				if ((positionCount + 4) % 3 != 0)
					return;

				BoardPosition[] positions = new BoardPosition[(positionCount + 4) / 3];
				int j = 0;
				for (int i = 0; i < positionCount; i++) {
					int col = input.readInt();
					int row = input.readInt();
					if (i == 0 || i == 1)
						positions[j++] = new BoardPosition(row, game.getColCount() - col - 1);
					else if (i % 3 == 0) {
						BoardPosition position = new BoardPosition(row, game.getColCount() - col - 1);
						if (!position.equals(positions[j - 1]))
							return;
					} else if (i % 3 == 1)
						positions[j++] = new BoardPosition(row, game.getColCount() - col - 1);
					else if (row != -1 && col != -1)
						return;
				}
				BoardMove move = new BoardMove(positions);

				if (DEBUG)
					System.out.println("The user " + user.getName() + " on the seat " + seat.getIndex() + " was sent a board move: " + move);

				if (!game.doMove(move)) {
					protocol = boardUser.prepareProtocol();
					protocol.adminChat("WARNING", "Invalid move");

					notifyGameState(boardUser);
				}

				break;
			case BoardInputProtocol.RESIGN:
				if (!game.isRunning())
					return;

				seat = getSeat(boardUser);
				if (seat == null)
					return;

				player = seat.getPlayer();
				if (player == null)
					return;

				player.resign();

				break;
			case BoardInputProtocol.SUGGEST_PAUSE_GAME:
				if (!game.isRunning())
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;

				int time = input.readInt();

				suggestPauseGame(seat, time);

				break;
			case BoardInputProtocol.ACCEPT_PAUSE_GAME:
				if (!game.isRunning())
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;

				acceptPauseGame(seat);

				break;
			case BoardInputProtocol.REJECT_PAUSE_GAME:
				if (!game.isRunning())
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;

				rejectPauseGame(seat);

				break;
			case BoardInputProtocol.OFFER_DRAW:
				if (!game.isRunning())
					return;
				if (game.isPaused())
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;

				whosOfferingDraw = seat;
				game.pause();

				protocol = (BoardOutputProtocol) preparePlayingBroadCastProtocol(seat);
				protocol.notifyPlayerOfferedDraw(user.getName(), user.getID());

				break;
			case BoardInputProtocol.ACCEPT_DRAW:
				if (!game.isRunning())
					return;
				if (!game.isPaused())
					return;
				if (whosOfferingDraw == null)
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;

				if (seat.equals(whosOfferingDraw))
					return;

				other = (BoardUser) whosOfferingDraw.getUser();
				if (other == null)
					return;

				protocol = other.prepareProtocol();
				protocol.notifyPlayerAcceptedDraw(boardUser.getName(), boardUser.getID());

				whosOfferingDraw = null;
				game.stop(StopReason.DRAW);

				break;
			case BoardInputProtocol.REJECT_DRAW:
				if (!game.isRunning())
					return;
				if (!game.isPaused())
					return;
				if (whosOfferingDraw == null)
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;

				if (seat.equals(whosOfferingDraw))
					return;

				other = (BoardUser) whosOfferingDraw.getUser();
				if (other == null)
					return;

				protocol = other.prepareProtocol();
				protocol.notifyPlayerRejectedDraw(boardUser.getName(), boardUser.getID());

				whosOfferingDraw = null;
				game.resume();

				break;
			case BoardInputProtocol.SUGGEST_UNDO_LAST_MOVE:
				if (!game.isRunning())
					return;
				if (game.isPaused())
					return;
				if (game.moveHistory().size() == 0)
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;
				player = seat.getPlayer();
				if (player == null || !player.equals(game.getPreviousPlayer()))
					return;

				whosSuggestedUndoMove = seat;
				game.pause();

				protocol = (BoardOutputProtocol) preparePlayingBroadCastProtocol(seat);
				protocol.notifyPlayerSuggestedUndoLastMove(user.getName(), user.getID());

				break;
			case BoardInputProtocol.ACCEPT_UNDO_LAST_MOVE:
				if (!game.isRunning())
					return;
				if (!game.isPaused())
					return;
				if (whosSuggestedUndoMove == null)
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;

				if (seat.equals(whosSuggestedUndoMove))
					return;

				other = (BoardUser) whosSuggestedUndoMove.getUser();
				if (other == null)
					return;

				protocol = other.prepareProtocol();
				protocol.notifyPlayerAcceptedUndoMove(boardUser.getName(), boardUser.getID());

				whosSuggestedUndoMove = null;
				game.resume();
				game.undoLastMove();

				break;
			case BoardInputProtocol.REJECT_UNDO_LAST_MOVE:
				if (!game.isRunning())
					return;
				if (!game.isPaused())
					return;
				if (whosSuggestedUndoMove == null)
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;
				if (seat.equals(whosSuggestedUndoMove))
					return;

				other = (BoardUser) whosSuggestedUndoMove.getUser();
				if (other == null)
					return;

				protocol = other.prepareProtocol();
				protocol.notifyPlayerRejectedUndoMove(boardUser.getName(), boardUser.getID());

				whosSuggestedUndoMove = null;
				game.resume();

				break;
			case BoardInputProtocol.RANDOM_MOVE:
				if (!activeHelp)
					return;
				if (!game.isRunning())
					return;
				if (game.isPaused())
					return;

				seat = getSeat(boardUser);
				if (seat == null || !seat.isPlaying())
					return;

				player = seat.getPlayer();
				if (player == null || !player.isMyTurn())
					return;

				game.doRandomMove();

				break;
			case BoardInputProtocol.TIME_UP:
				// TODO implementar algo aqui se for nescessário
				break;
			default:
				super.parseDataInternal(boardUser, opcode, input);
		}
	}

	private void pauseGameCanceled() {
		if (pauseGameQuestion != null)
			synchronized (pauseGameQuestion) {
				if (!pauseGameQuestion.isOpen())
					return;

				pauseGameQuestion.close();

				OutputProtocol protocol = prepareBroadCastProtocol();
				if (protocol != null)
					protocol.questionCanceled();
			}
	}

	@Override
	protected synchronized void readConfig(JCheckersDataInputStream input) throws IOException {
		int newGameType = input.readChar();
		if (validateGameType(newGameType)) {
			gameType = newGameType;
			updateGameType();
		}

		int newPrivacy = input.readChar();
		if (validatePrivacy(newPrivacy))
			privacy = newPrivacy;

		rated = input.readBoolean();
		hasRatingLimit = input.readBoolean();
		int newMinRating = input.readInt();
		if (validateMinRating(newMinRating))
			minRating = newMinRating;

		int newMaxRating = input.readInt();
		if (validateMaxRating(newMaxRating))
			maxRating = newMaxRating;

		BoardGame game = getGame();
		game.setUseTimePerTurn(input.readBoolean());
		game.setTimePerTurn(input.readInt());

		game.setUseIncrementTime(input.readBoolean());
		game.setIncrementTime(input.readInt());

		game.setUseTime(input.readBoolean());
		game.setInitialTime(input.readInt() * 60);

		int flag1 = input.readChar();
		noWatches = (flag1 & 1) != 0;
		input.readChar();
		input.readInt64();
		int flag2 = input.readChar();
		activeHelp = (flag2 & 1) != 0;
		swapSides = (flag2 & 2) != 0;
		input.readInt();
		input.readInt();
	}

	protected void rejectPauseGame(Seat seat) {
		if (pauseGameQuestion == null)
			return;

		synchronized (pauseGameQuestion) {
			if (!pauseGameQuestion.isOpen())
				return;

			if (seat.equals(pauseGameQuestion.getQuestioner())) {
				pauseGameCanceled();

				return;
			}

			if (!pauseGameQuestion.reject(seat))
				return;

			OutputProtocol protocol = preparePlayingBroadCastProtocol(seat);
			if (protocol != null) {
				User user = seat.getUser();
				((BoardOutputProtocol) protocol).notifyPlayerRejectedPauseGame(user.getName(), user.getID());
			}

			pauseGameCanceled();
		}
	}

	protected void suggestPauseGame(Seat seat, int time) {
		synchronized (this) {
			if (isClosing() || isClosed())
				return;
		}

		synchronized (pauseGameQuestion) {
			if (pauseGameQuestion.isOpen()) {
				acceptStopGame(seat);

				return;
			}

			List<Seat> questioneds = playingSeats();
			questioneds.remove(seat);
			User user = seat.getUser();
			pauseGameQuestion.open(time, seat, questioneds, (question, accepteds) -> onPauseGameTimeOut(question, accepteds));
			OutputProtocol protocol = prepareBroadCastProtocol(user);
			if (protocol != null)
				protocol.questionBits(1 << seat.getIndex(), 0);
			protocol = prepareSeatBroadCastProtocol(questioneds);
			if (protocol != null)
				((BoardOutputProtocol) protocol).notifyPlayerSuggestedPauseGame(user.getName(), user.getID(), time);
		}
	}

	protected abstract void updateGameType();

	protected abstract boolean validateGameType(int gameType);

	protected boolean validateMaxRating(int maxRating) {
		BoardServer server = getServer();

		int min = server.getMinRating();
		int max = server.getMaxRating();

		if (maxRating < min || maxRating > max)
			return false;

		BoardUser host = getHost();
		if (host == null)
			return true;

		int rating = host.getRating();
		if (rating + RATING_MARGIN <= max)
			return rating <= maxRating - RATING_MARGIN;

		return true;
	}

	protected boolean validateMinRating(int minRating) {
		BoardServer server = getServer();

		int min = server.getMinRating();
		int max = server.getMaxRating();

		if (minRating < min || minRating > max)
			return false;

		BoardUser host = getHost();
		if (host == null)
			return true;

		int rating = host.getRating();
		if (rating - RATING_MARGIN >= min)
			return rating >= minRating + RATING_MARGIN;

		return true;
	}

	@Override
	protected boolean validatePrivacy(int privacy) {
		return super.validatePrivacy(privacy) || privacy == CHALLANGE;
	}

	@Override
	protected synchronized void writeConfig(JCheckersDataOutputStream output) {
		output.writeChar(gameType);
		output.writeChar(privacy);
		output.writeBoolean(rated);
		output.writeBoolean(hasRatingLimit);
		output.writeInt(minRating);
		output.writeInt(maxRating);

		BoardGame game = getGame();
		output.writeBoolean(game.hasTimePerTurn());
		output.writeInt(game.getTimePerTurn());
		output.writeBoolean(game.hasIncrementTime());
		output.writeInt(game.getIncrementTime());
		output.writeBoolean(game.hasTime());
		output.writeInt(game.getTime() / 60);

		output.writeChar((noWatches ? 1 : 0) | 4); // flags1 (byte1=escape=pausa
													// byte2=sem visitantes)
		output.writeChar(0);
		output.writeInt64(1);
		output.writeChar((activeHelp ? 1 : 0) | (swapSides ? 2 : 0)); // flags2
																		// (byte1=ajuda
																		// ativa
																		// byte2=mudar
																		// de
																		// lado)
		output.writeInt(3);
		output.writeInt(1);
	}

	@Override
	public void writeUser(User user, JCheckersDataOutputStream output) {
		super.writeUser(user, output);

		output.writeInt(((BoardUser) user).getRating());
	}

}
