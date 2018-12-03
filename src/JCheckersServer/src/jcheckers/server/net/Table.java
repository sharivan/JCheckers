package jcheckers.server.net;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import common.process.ProcessQueue;
import common.process.ReturnableProcess;
import common.process.timer.Timer;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.logic.Game.StopReason;
import jcheckers.common.logic.GameController;
import jcheckers.common.logic.GameListener;
import jcheckers.common.logic.MultiplayerGame;
import jcheckers.common.logic.Player;
import jcheckers.server.io.InputProtocol;
import jcheckers.server.io.OutputProtocol;

public abstract class Table extends Base {

	protected class Controller implements GameController {

		@Override
		public ProcessQueue getQueue() {
			return Table.this.getQueue();
		}

	}

	protected class InviteQuestion extends Question<User, String> {

		public static final int RESPONSE_INTERVAL = 30000; // 30 segundos

		public String getInvited() {
			return getQuestioned(0);
		}

		public void open(User questioner, String invited, QuestionTimeout<User, String> callback) {
			super.open(questioner, Arrays.asList(invited), RESPONSE_INTERVAL, callback);
		}

	}

	protected class Listener implements GameListener {
		@Override
		public void onChangeTurn(int turn) {
			notifyGameStateAll();
		}

		@Override
		public void onNextRound(int round) {

		}

		@Override
		public void onRotateLeft() {
			Seat first = seats[0];
			for (int index = 0; index < seats.length - 1; index++) {
				seats[index] = seats[index + 1];
				if (seats[index] != null)
					seats[index].index = index;
			}

			seats[seats.length - 1] = first;
			if (first != null)
				first.index = seats.length - 1;

			notifyChanged();
		}

		@Override
		public void onRotateRight() {
			Seat last = seats[seats.length - 1];
			for (int index = seats.length - 1; index > 0; index--) {
				seats[index] = seats[index - 1];
				if (seats[index] != null)
					seats[index].index = index;
			}

			seats[0] = last;
			if (last != null)
				last.index = 0;

			notifyChanged();
		}

		@Override
		public void onStart() {
			logToOut("The game #" + game.getGameNumber() + " has started");

			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.startGame();
		}

		@Override
		public void onStarted() {

		}

		@Override
		public void onStop(StopReason reason) {
			onStopGame(reason);
		}

		@Override
		public void onSwap(int index1, int index2) {
			Seat temp = seats[index1];
			seats[index1] = seats[index2];
			seats[index2] = temp;

			if (seats[index1] != null)
				seats[index1].index = index1;
			if (seats[index2] != null)
				seats[index2].index = index2;

			notifyChanged();
		}
	}

	protected abstract class Question<U, V> {

		public static final int DEFAULT_TIME_OUT = 10000; // 10 segundos

		public static final int NO_RESPONSE = 0;
		public static final int ACCEPTED = 1;
		public static final int REJECTED = 2;

		private U questioner;
		private List<V> questioneds;
		private QuestionTimeout<U, V> callback;

		private byte[] responses;
		private Timer tmrResponseExpires;
		private boolean closed;

		protected Question() {
			questioner = null;
			questioneds = null;
			callback = null;
			responses = null;
			closed = true;

			tmrResponseExpires = new Timer(getQueue(), DEFAULT_TIME_OUT, true, (e) -> server.logToErr(Table.this.toString(), null, e));
			tmrResponseExpires.addListener((timer, interval) -> {
				tmrResponseExpires.pause();

				if (Question.this.callback != null)
					Question.this.callback.onTimeout(Question.this, accepteds());
			});
		}

		public synchronized boolean accept(V questioned) {
			if (closed || questioned.equals(questioner))
				return false;

			int index = questioneds.indexOf(questioned);
			if (index == -1)
				return false;

			responses[index] = ACCEPTED;

			return true;
		}

		private synchronized List<V> accepteds() {
			ArrayList<V> result = new ArrayList<>();

			if (closed)
				return result;

			for (int i = 0; i < responses.length; i++)
				if (responses[i] == ACCEPTED)
					result.add(questioneds.get(i));

			return result;
		}

		public synchronized boolean allAccepted() {
			return getAcceptedCount() == getQuestionedCount();
		}

		public synchronized boolean allOpined() {
			return getOpinedCount() == getQuestionedCount();
		}

		public synchronized boolean allRejected() {
			return getRejectedCount() == getQuestionedCount();
		}

		public synchronized void close() {
			if (closed)
				return;

			callback = null;
			questioneds = null;
			questioner = null;
			responses = null;
			tmrResponseExpires.pause();

			closed = true;
		}

		public synchronized int getAcceptedCount() {
			int result = 0;

			for (int i = 0; i < getQuestionedCount(); i++)
				if (getResponse(i) == ACCEPTED)
					result++;

			return result;
		}

		public synchronized int getOpinedCount() {
			int result = 0;

			for (int i = 0; i < getQuestionedCount(); i++)
				if (getResponse(i) != NO_RESPONSE)
					result++;

			return result;
		}

		public synchronized V getQuestioned(int index) {
			return !closed ? questioneds.get(index) : null;
		}

		public synchronized int getQuestionedCount() {
			return !closed ? questioneds.size() : 0;
		}

		public synchronized U getQuestioner() {
			return questioner;
		}

		public synchronized int getRejectedCount() {
			int result = 0;

			for (int i = 0; i < getQuestionedCount(); i++)
				if (getResponse(i) == REJECTED)
					result++;

			return result;
		}

		public synchronized int getResponse(int index) {
			return responses[index];
		}

		public synchronized int indexOfQuestioned(V questioned) {
			return !closed ? questioneds.indexOf(questioned) : -1;
		}

		public synchronized boolean isOpen() {
			return !closed;
		}

		public synchronized void open(U questioner, List<V> questioneds, int timeOut, QuestionTimeout<U, V> callback) {
			if (!closed)
				return;

			this.questioner = questioner;
			this.questioneds = questioneds;
			this.callback = callback;

			closed = false;

			responses = new byte[questioneds.size()];
			for (int i = 0; i < responses.length; i++)
				responses[i] = NO_RESPONSE;

			tmrResponseExpires.setInterval(timeOut);
			tmrResponseExpires.reset();
			tmrResponseExpires.play();
		}

		public synchronized boolean reject(V questioned) {
			if (closed || questioned.equals(questioner))
				return false;

			int index = questioneds.indexOf(questioned);
			if (index == -1)
				return false;

			responses[index] = REJECTED;

			return true;
		}

		public synchronized void terminate() {
			close();

			try {
				tmrResponseExpires.close();
			} finally {
				tmrResponseExpires = null;
			}
		}

		public synchronized boolean wasQuestioned(V questioned) {
			return questioneds.contains(questioned);
		}

	}

	protected interface QuestionTimeout<U, V> {

		void onTimeout(Question<U, V> question, List<V> accepteds);

	}

	public abstract class Seat {

		private int index;
		private User user;
		private boolean focused;
		private Player player;
		private boolean leaving;
		private boolean wantToStart;

		protected Seat(int index, User user) {
			this.index = index;
			this.user = user;

			focused = true;
			wantToStart = false;
		}

		public final synchronized boolean canStart() {
			return user != null && !leaving && game.canJoin(index, user.getName());
		}

		private synchronized void checkClosed() {
			if (user == null)
				throw new RuntimeException("The seat " + index + " is currently closed.");
		}

		public int getIndex() {
			checkClosed();

			return index;
		}

		public synchronized Player getPlayer() {
			checkClosed();

			return player;
		}

		public synchronized User getUser() {
			checkClosed();

			return user;
		}

		public synchronized boolean isFocused() {
			checkClosed();

			return focused;
		}

		protected synchronized boolean isLeaving() {
			return leaving;
		}

		public synchronized boolean isPlaying() {
			checkClosed();

			return player != null && player.isPlaying();
		}

		protected synchronized void leave() {
			if (leaving || user == null)
				return;

			leaving = true;
			try {
				if (DEBUG)
					System.out.println("Seat [" + index + "] " + user + " leaving");

				if (player != null)
					player.leave();
			} finally {
				player = null;
				user = null;
				leaving = false;
			}
		}

		public synchronized void setFocused(boolean value) {
			checkClosed();

			if (focused == value)
				return;

			focused = value;

			notifyFocus();
		}

		public final void standUp() {
			checkClosed();

			post(() -> standUpInternal(index, false));
		}

		public final synchronized boolean start() {
			checkClosed();

			player = game.join(index, user.getName());
			if (DEBUG)
				System.out.println("Seat [" + index + "] " + user + " started the game and received the player descriptor: " + player);

			if (player != null)
				return player.start();

			return false;
		}

		@Override
		public String toString() {
			return user + " at #" + index;
		}

		public boolean wantToStart() {
			return wantToStart;
		}

		public synchronized void write(JCheckersDataOutputStream output) {
			checkClosed();

			writeUser(user, output);
			output.writeInt(user.getReconnectingTime());
		}

	}

	protected class SeatBroadCastOutputStream extends JCheckersDataOutputStream {

		ByteArrayOutputStream out;
		List<Seat> seats;
		Seat ignore;

		private SeatBroadCastOutputStream(ByteArrayOutputStream out) {
			this(out, null, null);
		}

		public SeatBroadCastOutputStream(ByteArrayOutputStream out, List<Seat> seats) {
			this(out, seats, null);
		}

		public SeatBroadCastOutputStream(ByteArrayOutputStream out, List<Seat> seats, Seat ignore) {
			super(out);

			this.out = out;
			this.seats = seats;
			this.ignore = ignore;
		}

		private SeatBroadCastOutputStream(ByteArrayOutputStream out, Seat ignore) {
			this(out, null, ignore);
		}

		@Override
		public void close() {
			super.close();

			out = null;
			ignore = null;
		}

		@Override
		protected void flushInternal(boolean close) {
			byte[] buf = out.toByteArray();
			synchronized (Table.this.seats) {
				for (Seat seat : Table.this.seats) {
					if (seat == null)
						continue;

					User user = seat.getUser();
					if (user == null || user.isReconnecting())
						continue;

					if (ignore != null && ignore.equals(seat))
						continue;

					if (seats != null && !seats.contains(seat))
						continue;

					user.postBlock(buf, close);
				}
			}
		}

	}

	protected class SeatQuestion extends Question<Seat, Seat> {

		public static final int RESPONSE_INTERVAL = 15000; // 15 segundos

		public SeatQuestion() {
			super();
		}

		public synchronized int getAcceptedBits() {
			Seat questioner = getQuestioner();
			int result = questioner != null ? 1 << questioner.getIndex() : 0;

			for (int i = 0; i < getQuestionedCount(); i++) {
				Seat questioned = getQuestioned(i);
				if (getResponse(i) == ACCEPTED)
					result |= 1 << questioned.getIndex();
			}

			return result;
		}

		public synchronized int getRejectedBits() {
			int result = 0;

			for (int i = 0; i < getQuestionedCount(); i++) {
				Seat questioned = getQuestioned(i);
				if (getResponse(i) == REJECTED)
					result |= 1 << questioned.getIndex();
			}

			return result;
		}

		public void open(Seat questioner, List<Seat> questioneds, QuestionTimeout<Seat, Seat> callback) {
			super.open(questioner, questioneds, RESPONSE_INTERVAL, callback);
		}

	}

	private static final boolean DEBUG = false;

	private static AtomicInteger ID = new AtomicInteger(1000);

	public static final int PUBLIC = 0;
	public static final int PRIVATE = 1;

	protected static final int NO_REASON = 0;
	protected static final int NO_ENOUGH_PLAYERS = 1;

	private Room room;
	private int number;

	protected int privacy;
	protected boolean noWatches;

	private int oldPrivacy;
	private boolean oldNoWatches;

	private User host;
	private int id;

	protected Seat[] seats;

	protected Vector<User> watchers;
	private Vector<String> booteds;
	private Vector<InviteQuestion> inviteds;

	protected SeatQuestion startGameQuestion;
	protected SeatQuestion stopGameQuestion;
	private SeatQuestion continueGameQuestion;

	private MultiplayerGame game;

	protected Table() {

	}

	protected void acceptStartGame(Seat seat) {
		seat.wantToStart = true;

		if (continueGameQuestion == null)
			return;

		synchronized (continueGameQuestion) {
			if (continueGameQuestion.isOpen()) {
				if (!continueGameQuestion.accept(seat))
					return;

				OutputProtocol protocol = prepareBroadCastProtocol();
				if (protocol != null)
					protocol.questionBits(continueGameQuestion.getAcceptedBits(), continueGameQuestion.getRejectedBits());

				if (continueGameQuestion.allOpined() && continueGameQuestion.getAcceptedCount() > 1) {
					List<Seat> accepteds = new ArrayList<>();
					for (int i = 0; i < continueGameQuestion.getQuestionedCount(); i++)
						accepteds.add(continueGameQuestion.getQuestioned(i));

					continueGameQuestion.close();

					startGame(null, accepteds);
				}

				return;
			}
		}

		if (startGameQuestion == null)
			return;

		synchronized (startGameQuestion) {
			if (!startGameQuestion.isOpen() || !startGameQuestion.accept(seat))
				return;

			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.questionBits(startGameQuestion.getAcceptedBits(), startGameQuestion.getRejectedBits());

			if (startGameQuestion.allOpined() && startGameQuestion.getAcceptedCount() > 0) {
				Seat questioner = startGameQuestion.getQuestioner();
				List<Seat> accepteds = new ArrayList<>();
				accepteds.add(startGameQuestion.getQuestioner());
				for (int i = 0; i < startGameQuestion.getQuestionedCount(); i++)
					accepteds.add(startGameQuestion.getQuestioned(i));

				startGameQuestion.close();

				startGame(questioner, accepteds);
			}
		}
	}

	protected void acceptStopGame(Seat seat) {
		if (stopGameQuestion == null)
			return;

		synchronized (stopGameQuestion) {
			if (!stopGameQuestion.isOpen() || !stopGameQuestion.accept(seat))
				return;

			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.questionBits(stopGameQuestion.getAcceptedBits(), 0);

			if (stopGameQuestion.allAccepted()) {
				stopGameQuestion.close();

				game.stop(StopReason.CANCELED);
			}
		}
	}

	@Override
	protected void afterClose() {
		super.afterClose();

		room = null;
		host = null;
		game = null;
		startGameQuestion = null;
		stopGameQuestion = null;
		continueGameQuestion = null;
	}

	@Override
	protected void beforeClose() {
		super.beforeClose();

		game.stop(StopReason.CANCELED);

		synchronized (seats) {
			for (int i = 0; i < seats.length; i++)
				standUpInternal(i, true);
		}

		game.close();

		watchers.clear();
		booteds.clear();
		for (InviteQuestion question : inviteds)
			question.terminate();
		inviteds.clear();

		startGameQuestion.terminate();
		stopGameQuestion.terminate();
		continueGameQuestion.terminate();

		room.closeTable(this);
	}

	protected abstract MultiplayerGame createGame();

	protected abstract Player[] createPlayers();

	protected abstract Seat createSeat(int seatIndex, User user);

	protected abstract Seat[] createSeats();

	protected void defaultConfig() {
		privacy = PUBLIC;
		noWatches = false;
	}

	public synchronized int getCurrentTime() {
		return game.getCurrentTimePerTurn();
	}

	public int getFocusedBits() {
		synchronized (seats) {
			int result = 0;
			for (int i = 0; i < seats.length; i++)
				if (seats[i] != null && seats[i].isFocused() && !seats[i].getUser().isReconnecting())
					result |= 1 << i;

			return result;
		}
	}

	protected MultiplayerGame getGame() {
		return game;
	}

	protected abstract int getGameID();

	public synchronized User getHost() {
		return host;
	}

	public int getID() {
		return id;
	}

	protected InviteQuestion getInviteQuestion(String name) {
		synchronized (inviteds) {
			for (InviteQuestion invited : inviteds)
				if (invited.wasQuestioned(name))
					return invited;

			return null;
		}
	}

	@Override
	public File getLogDir() {
		String homeDir = server.getHomeDir();
		String logDir = server.getLogDir();
		File home = new File(homeDir);
		File file = new File(logDir);
		if (!file.isAbsolute())
			file = new File(home, logDir);
		file = new File(file, "rooms");
		file = new File(file, "tables");

		return file;
	}

	public abstract int getMaxSeatCount();

	public int getNumber() {
		return number;
	}

	public int getReconnectBits() {
		synchronized (seats) {
			int result = 0;
			for (int i = 0; i < seats.length; i++)
				if (seats[i] != null && seats[i].getUser().isReconnecting())
					result |= 1 << i;

			return result;
		}
	}

	@Override
	public final Room getRoom() {
		return room;
	}

	public Seat getSeat(User user) {
		synchronized (seats) {
			int seatIndex = getSeatIndex(user);
			if (seatIndex == -1)
				return null;

			return seats[seatIndex];
		}
	}

	public int getSeatBits() {
		synchronized (seats) {
			int result = 0;
			for (int i = 0; i < seats.length; i++)
				if (seats[i] != null)
					result |= 1 << i;

			return result;
		}
	}

	public int getSeatCount() {
		synchronized (seats) {
			int result = 0;
			for (int i = 0; i < seats.length; i++)
				if (seats[i] != null)
					result++;

			return result;
		}
	}

	public int getSeatIndex(User user) {
		synchronized (seats) {
			for (int i = 0; i < seats.length; i++)
				if (seats[i] != null && user.getName().equalsIgnoreCase(seats[i].getUser().getName()))
					return i;

			return -1;
		}
	}

	public int getTime() {
		return game.getTimePerTurn();
	}

	@Override
	public User getUserByIDExt(int id) {
		User result = getUserByID(id);
		if (result != null)
			return result;

		return room.getUserByID(id);
	}

	public boolean isNoWatches() {
		return noWatches;
	}

	public boolean join(User user, int seatIndex, int mode) {
		if (mode == InputProtocol.CREATE_TABLE)
			throw new RuntimeException("Invalid mode CREATE_TABLE.");

		return send((ReturnableProcess<Boolean>) () -> joinPrivileged(user, seatIndex, mode));
	}

	protected boolean joinPrivileged(User user, int seatIndex, int mode) {
		boolean allSeatsOccupied = getSeatCount() == seats.length;
		boolean isValidSeatIndex = (mode == InputProtocol.JOIN_TABLE_SITING || mode == InputProtocol.JOIN_TABLE_RECONNECTED_SITING) && seatIndex >= 0 && seatIndex < seats.length;
		boolean reconnecting = isReconnecting(user.getName());

		InviteQuestion question = getInviteQuestion(user.getName());

		if (mode != InputProtocol.CREATE_TABLE) {
			if (booteds.contains(user.getName())) {
				OutputProtocol protocol = user.prepareProtocol();
				if (protocol == null)
					return false;

				protocol.connectToTheTableRefused();

				return false;
			}

			if ((mode == InputProtocol.JOIN_TABLE_RECONNECTED_SITING || mode == InputProtocol.JOIN_TABLE_RECONNECTED_WATCHING) && !reconnecting) {
				OutputProtocol protocol = user.prepareProtocol();
				if (protocol == null)
					return false;

				protocol.couldReconnectToTheTableTryAgainLatter();

				return false;
			}

			if (!reconnecting) {
				if (privacy == PRIVATE && question == null) {
					OutputProtocol protocol = user.prepareProtocol();
					if (protocol == null)
						return false;

					protocol.privateTable();

					return false;
				}

				if (noWatches && !isValidSeatIndex) {
					OutputProtocol protocol = user.prepareProtocol();
					if (protocol == null)
						return false;

					protocol.watchersNotAllowed();

					return false;
				}
			}
		}

		if (!super.addUser(user)) {
			OutputProtocol protocol = user.prepareProtocol();
			if (protocol == null)
				return false;

			protocol.alreadyConnectedOnTheTable();

			return false;
		}

		if (question != null) {
			question.accept(user.getName());
			inviteds.remove(question);
		}

		if (mode == InputProtocol.CREATE_TABLE) {
			logToOut("Table " + number + " created by the user " + user.getName() + " [compid=" + user.getCompID() + " ip=" + user.getIP().getHostAddress() + "]");
			sit(0, user);
		} else if (reconnecting)
			logToOut("User " + user.getName() + " reconnected [compid=" + user.getCompID() + " ip=" + user.getIP().getHostAddress() + "]");
		else {
			logToOut("User " + user.getName() + " joined [compid=" + user.getCompID() + " ip=" + user.getIP().getHostAddress() + "]");
			if (isValidSeatIndex && !allSeatsOccupied)
				synchronized (seats) {
					if (seats[seatIndex] != null)
						for (int i = 0; i < seats.length; i++)
							if (seats[i] == null) {
								seatIndex = i;

								break;
							}

					sit(seatIndex, user);
				}
			else
				watchers.add(user);
		}

		OutputProtocol protocol = prepareBroadCastProtocol();
		if (protocol != null)
			protocol.playerConnect(user.getName(), user.getID());

		if (host == null) {
			host = user;
			logToOut("User " + user.getName() + " now is the host");

			protocol = user.prepareProtocol();
			if (protocol == null)
				return false;

			protocol.youReceivedTheHostFromTheServer();
		}

		if (room != null)
			room.postUserList(user);

		notifyChanged();
		notifyAvatars();
		notifyFocus();
		notifyGameState(user);

		protocol = user.prepareProtocol();
		if (protocol == null)
			return false;

		protocol.wellcome(room.getName());

		return true;
	}

	private User nextHost() {
		synchronized (seats) {
			for (int i = 0; i < seats.length; i++) {
				if (seats[i] == null)
					continue;

				User user = seats[i].getUser();
				if (user != null && user.isConnected())
					return user;
			}
		}

		synchronized (watchers) {
			if (watchers.size() > 0)
				return watchers.get(0);
		}

		return null;
	}

	@Override
	protected void notifyAvatars() {
		synchronized (seats) {
			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.avatars(seats);
		}
	}

	public void notifyChanged() {
		post(() -> {
			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.updateTable(this);
			protocol = room.prepareBroadCastProtocol();
			if (protocol != null)
				protocol.updateTable(this);
		});
	}

	private void notifyFocus() {
		prepareBroadCastProtocol().tableFocus(getFocusedBits());
	}

	protected abstract void notifyGameState(User user);

	protected final void notifyGameStateAll() {
		List<User> users = users();
		synchronized (users) {
			for (User user : users)
				notifyGameState(user);
		}
	}

	protected void notifySeatsWithSameIP(List<Seat> seats) {
		HashMap<String, List<String>> ips = new HashMap<>();
		for (Seat seat : seats) {
			String ip = seat.getUser().getIP().getHostAddress();
			List<String> players = ips.get(ip);
			if (players == null) {
				players = new ArrayList<>();
				ips.put(ip, players);
			}
			players.add(seat.getUser().getName());
		}

		Set<String> keys = ips.keySet();
		for (String ip : keys) {
			List<String> players = ips.get(ip);
			if (players.size() > 1) {
				String message = "The players " + players.get(0);
				for (int i = 1; i < players.size(); i++)
					message += ", " + players.get(i);
				logToOut(message + " have the same ip " + ip);

				OutputProtocol protocol = prepareBroadCastProtocol();
				if (protocol != null)
					protocol.playersInTheSameLocalNetwork(players);
			}
		}
	}

	private void onContinueGameTimeOut(Question<Seat, Seat> question, List<Seat> accepteds) {
		post(() -> {
			if (continueGameQuestion == null)
				return;

			continueGameQuestion.close();

			if (accepteds.size() > 1)
				startGame(null, accepteds);
			else
				onStartGameCanceled(null, NO_ENOUGH_PLAYERS);
		});
	}

	protected void onSeatCantStart(Seat seat) {

	}

	protected void onStartGameCanceled(Seat starter, int reason) {
		for (Seat seat : seats)
			if (seat != null)
				seat.wantToStart = false;

		if (reason == NO_ENOUGH_PLAYERS && starter != null) {
			OutputProtocol protocol = starter.getUser().prepareProtocol();
			if (protocol != null)
				protocol.noEnoughPlayersToStartTheGame();
		}

		OutputProtocol protocol = prepareBroadCastProtocol();
		if (protocol != null)
			protocol.questionCanceled();
	}

	private void onStartGameTimeOut(Question<Seat, Seat> question, List<Seat> accepteds) {
		post(() -> {
			if (startGameQuestion == null)
				return;

			Seat questioner;
			synchronized (startGameQuestion) {
				if (!startGameQuestion.isOpen())
					return;

				questioner = startGameQuestion.getQuestioner();
				startGameQuestion.close();
			}

			if (accepteds.size() > 0) {
				accepteds.add(questioner);
				startGame(questioner, accepteds);
			} else
				onStartGameCanceled(questioner, NO_ENOUGH_PLAYERS);
		});
	}

	protected void onStopGame(StopReason reason) {
		for (Seat seat : seats)
			if (seat != null)
				seat.wantToStart = false;

		logToOut("The game #" + game.getGameNumber() + " has " + (reason == StopReason.CANCELED ? "canceled" : "stoped"));

		stopGameCanceled();

		notifyGameStateAll();

		OutputProtocol protocol = prepareBroadCastProtocol();
		if (protocol != null)
			protocol.stopGame();

		if (isOpen() && reason != StopReason.CANCELED && getSeatCount() > 1) {
			List<Seat> questioneds = seatsThatCanStart();
			if (questioneds.size() > 1) {
				notifySeatsWithSameIP(questioneds);
				continueGameQuestion.open(null, questioneds, (question, accepteds) -> onContinueGameTimeOut(question, accepteds));

				protocol = prepareSeatBroadCastProtocol(questioneds);
				if (protocol != null)
					protocol.continueGame();
			}
		}
	}

	private void onStopGameTimeOut(Question<Seat, Seat> question, List<Seat> accepteds) {
		post(() -> stopGameCanceled());
	}

	protected void onTableRefreshParameters(JCheckersDataInputStream input) throws IOException {
		readConfig(input);
	}

	protected void open(Server server, Room lobby, int number, JCheckersDataInputStream input) throws IOException {
		room = lobby;
		this.number = number;

		id = ID.getAndUpdate((operand) -> {
			operand++;
			if (operand == 0)
				operand = 1000;
			return operand;
		});

		super.open(new ThreadGroup(lobby.getGroup(), toString()), server);

		host = null;

		seats = createSeats();
		watchers = new Vector<>();
		booteds = new Vector<>();
		inviteds = new Vector<>();
		game = createGame();

		startGameQuestion = new SeatQuestion();
		stopGameQuestion = new SeatQuestion();
		continueGameQuestion = new SeatQuestion();

		defaultConfig();

		int gameID = input.readUChar();

		readConfig(input);

		getQueue().setName("Table queue [" + lobby.getName() + "-" + number + "]");

		if (gameID != getGameID())
			throw new IOException("The game id shold be " + getGameID() + " and not " + gameID);
	}

	@Override
	protected void parseDataInternal(User user, int opcode, JCheckersDataInputStream input) throws IOException {
		String name;
		Seat seat;
		int seatIndex;
		@SuppressWarnings("unused")
		int seatID;
		int id;
		OutputProtocol protocol;
		InviteQuestion question;
		switch (opcode) {
			case InputProtocol.GOT_FOCUS:
			case InputProtocol.LOST_FOCUS:
				seat = getSeat(user);
				if (seat == null)
					return;

				seat.setFocused(opcode == InputProtocol.GOT_FOCUS);

				break;
			case InputProtocol.BOOT:
				id = input.readInt();

				if (!user.equals(host)) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youAreNotTheHostToKickPlayers();

					return;
				}

				User booted = getUserByID(id);
				if (booted == null) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youCantKickAPlayerNoInServer();

					return;
				}

				seat = getSeat(booted);
				if (seat != null && seat.isPlaying())
					return;

				if (!booteds.contains(booted.getName()))
					booteds.add(booted.getName());

				logToOut(booted.getName() + " was kicked by " + user.getName());

				protocol = prepareBroadCastProtocol(booted);
				if (protocol != null)
					protocol.otherWasKickedByTheHost(booted.getName(), id, user.getName(), user.getID());

				protocol = booted.prepareProtocol();
				if (protocol == null)
					return;

				protocol.youWasKickedByTheHost(user.getName(), user.getID());

				booted.close();

				break;
			case InputProtocol.INVITE:
				id = input.readInt();

				if (!user.equals(host)) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youAreNotTheHostToInvitePlayers();

					return;
				}

				User invited = room.getUserByID(id);
				if (invited == null) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youCantInviteAPlayerNotInServer();

					return;
				}

				if (containsUser(invited.getName())) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youCantInviteAPlayerAlreadyInTheTable();

					return;
				}

				if (invited.nameEquals(user)) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youCantInviteYourself();

					return;
				}

				question = getInviteQuestion(invited.getName());
				if (question != null)
					return;

				logToOut(invited.getName() + " was invited by " + user.getName());

				question = new InviteQuestion();
				inviteds.add(question);
				question.open(user, invited.getName(), (sender, accepteds) -> {
					OutputProtocol protocol1 = invited.prepareProtocol();
					if (protocol1 == null)
						return;

					protocol1.inviationsInactive();

					sender.terminate();
					inviteds.remove(sender);
				});
				booteds.remove(invited.getName());

				protocol = invited.prepareProtocol();
				if (protocol == null)
					return;

				protocol.invited(user.getName(), user.getID(), this.id, 0, user, number);

				protocol = user.prepareProtocol();
				if (protocol == null)
					return;

				protocol.inviationSent(invited.getName(), invited.getID());

				break;
			case InputProtocol.TRANSFER_HOST:
				id = input.readInt();

				if (!user.equals(host)) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youAreNotTheHostToTransferHost();

					return;
				}

				User newHost = getUserByID(id);
				if (newHost == null) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youCantTransferHostToAPlayerNotInServer();

					return;
				}

				if (newHost.equals(user)) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youCantTransferHostToYourself();

					return;
				}

				logToOut(newHost.getName() + " was received the host by " + user.getName());

				host = newHost;

				protocol = prepareBroadCastProtocol(newHost);
				if (protocol != null)
					protocol.otherReceivedTheHost(newHost.getName(), newHost.getID(), user.getName(), user.getID());

				protocol = newHost.prepareProtocol();
				if (protocol == null)
					return;

				protocol.youReceivedTheHost(user.getName(), user.getID());

				notifyChanged();

				break;
			case InputProtocol.SIT:
				seatIndex = input.readInt();
				seatID = input.readInt();

				sitInternal(seatIndex, user);

				break;
			case InputProtocol.STAND_UP:
				seatID = input.readInt();

				seat = getSeat(user);
				if (seat == null)
					return;

				standUpInternal(seat.getIndex(), false);

				break;
			case InputProtocol.SUGGEST_START_GAME:
				if (game.isRunning())
					return;

				seat = getSeat(user);
				if (seat == null)
					return;

				suggestStartGame(seat);

				break;
			case InputProtocol.START_GAME_SUGGEST_ACCEPTED:
				if (game.isRunning())
					return;

				seat = getSeat(user);
				if (seat == null)
					return;

				acceptStartGame(seat);

				break;
			case InputProtocol.START_GAME_SUGGEST_REJECTED:
				if (game.isRunning())
					return;

				seat = getSeat(user);
				if (seat == null)
					return;

				rejectStartGame(seat);

				break;
			case InputProtocol.SUGGEST_STOP_GAME:
				if (!game.isRunning())
					return;

				seat = getSeat(user);
				if (seat == null || !seat.isPlaying())
					return;

				suggestStopGame(seat);

				break;
			case InputProtocol.STOP_GAME_SUGGEST_ACCEPTED:
				if (!game.isRunning())
					return;

				seat = getSeat(user);
				if (seat == null || !seat.isPlaying())
					return;

				acceptStopGame(seat);

				break;
			case InputProtocol.STOP_GAME_SUGGEST_REJECTED:
				if (!game.isRunning())
					return;

				seat = getSeat(user);
				if (seat == null || !seat.isPlaying())
					return;

				rejectStopGame(seat);

				break;
			case InputProtocol.DONT_CONTINUE_GAME:
				if (game.isRunning())
					return;

				seat = getSeat(user);
				if (seat == null)
					return;

				rejectStartGame(seat);

				break;
			case InputProtocol.CONTINUE_GAME:
				if (getGame().isRunning())
					return;

				seat = getSeat(user);
				if (seat == null)
					return;

				acceptStartGame(seat);

				break;
			case InputProtocol.INVITATION_REJECTED:
				name = input.readString();
				id = input.readInt();

				User other = getUserByID(id);
				if (other == null)
					return;

				protocol = other.prepareProtocol();
				if (protocol == null)
					return;

				protocol.inviationRejected(name, id);

				break;
			case InputProtocol.INVITATION_AUTO_REJECTED:
				name = input.readString();
				id = input.readInt();

				other = getUserByID(id);
				if (other == null)
					return;

				protocol = other.prepareProtocol();
				if (protocol == null)
					return;

				protocol.inviationAutoRejected(name, id);

				break;
			case InputProtocol.CHANGE_TABLE_PARAMETERS:
				if (!user.equals(host)) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youAreNotTheHostToChangeParameters();

					return;
				}

				if (game.isRunning()) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.youCantChangeParametersNow();

					return;
				}

				logToOut(user.getName() + " has changed the table parameters");

				startGameCanceled();

				saveConfig();
				try {
					onTableRefreshParameters(input);
				} catch (IOException e) {
					rollBackConfig();

					throw e;
				}

				protocol = prepareBroadCastProtocol();
				if (protocol != null)
					protocol.configChanged(user.getName(), user.getID());

				notifyChanged();
				notifyGameStateAll();

				break;
			default:
				super.parseDataInternal(user, opcode, input);
		}
	}

	public List<Player> players() {
		synchronized (seats) {
			ArrayList<Player> result = new ArrayList<>();
			for (Seat seat : seats)
				if (seat != null && seat.getPlayer() != null)
					result.add(seat.getPlayer());

			return result;
		}
	}

	public List<Seat> playingSeats() {
		synchronized (seats) {
			ArrayList<Seat> result = new ArrayList<>();
			for (Seat seat : seats)
				if (seat != null && seat.isPlaying())
					result.add(seat);

			return result;
		}
	}

	protected void postState(User user) {
		synchronized (this) {
			if (isClosing() || isClosed())
				return;
		}

		OutputProtocol protocol = user.prepareProtocol();
		if (protocol == null)
			return;

		protocol.updateTable(this);
	}

	public OutputProtocol preparePlayingBroadCastProtocol() {
		return server.createOutputProtocol(prepareSeatBroadCast(playingSeats()));
	}

	public OutputProtocol preparePlayingBroadCastProtocol(Seat ignore) {
		return server.createOutputProtocol(prepareSeatBroadCast(playingSeats(), ignore));
	}

	public JCheckersDataOutputStream prepareSeatBroadCast() {
		return new SeatBroadCastOutputStream(new ByteArrayOutputStream());
	}

	public JCheckersDataOutputStream prepareSeatBroadCast(List<Seat> seats) {
		return new SeatBroadCastOutputStream(new ByteArrayOutputStream(), seats);
	}

	public JCheckersDataOutputStream prepareSeatBroadCast(List<Seat> seats, Seat ignore) {
		return new SeatBroadCastOutputStream(new ByteArrayOutputStream(), seats, ignore);
	}

	public JCheckersDataOutputStream prepareSeatBroadCast(Seat ignore) {
		return new SeatBroadCastOutputStream(new ByteArrayOutputStream(), ignore);
	}

	public OutputProtocol prepareSeatBroadCastProtocol() {
		return server.createOutputProtocol(prepareSeatBroadCast());
	}

	public OutputProtocol prepareSeatBroadCastProtocol(List<Seat> seats) {
		return server.createOutputProtocol(prepareSeatBroadCast(seats));
	}

	public OutputProtocol prepareSeatBroadCastProtocol(List<Seat> seats, Seat ignore) {
		return server.createOutputProtocol(prepareSeatBroadCast(seats, ignore));
	}

	public OutputProtocol prepareSeatBroadCastProtocol(Seat ignore) {
		return server.createOutputProtocol(prepareSeatBroadCast(ignore));
	}

	public OutputProtocol prepareStartedBroadCastProtocol() {
		return server.createOutputProtocol(prepareSeatBroadCast(startedSeats()));
	}

	public OutputProtocol prepareStartedBroadCastProtocol(Seat ignore) {
		return server.createOutputProtocol(prepareSeatBroadCast(startedSeats(), ignore));
	}

	public OutputProtocol prepareStartedButNotPlayingBroadCastProtocol() {
		return server.createOutputProtocol(prepareSeatBroadCast(startedButNotPlayingSeats()));
	}

	public OutputProtocol prepareStartedButNotPlayingBroadCastProtocol(Seat ignore) {
		return server.createOutputProtocol(prepareSeatBroadCast(startedButNotPlayingSeats(), ignore));
	}

	protected synchronized void readConfig(JCheckersDataInputStream input) throws IOException {
		input.readChar();

		int newPrivacy = input.readInt();
		if (validatePrivacy(newPrivacy))
			privacy = newPrivacy;

		input.readBoolean();

		game.setTimePerTurn(input.readInt());

		noWatches = input.readBoolean();
	}

	protected void rejectStartGame(Seat seat) {
		seat.wantToStart = false;

		if (continueGameQuestion == null)
			return;

		synchronized (continueGameQuestion) {
			if (continueGameQuestion.isOpen()) {
				if (!continueGameQuestion.reject(seat))
					return;

				if (continueGameQuestion.getRejectedCount() == continueGameQuestion.getQuestionedCount() - 1) {
					continueGameQuestion.close();

					onStartGameCanceled(null, NO_REASON);
				} else if (continueGameQuestion.allOpined() && continueGameQuestion.getAcceptedCount() > 1) {
					List<Seat> accepteds = new ArrayList<>();
					for (int i = 0; i < continueGameQuestion.getQuestionedCount(); i++)
						accepteds.add(continueGameQuestion.getQuestioned(i));

					continueGameQuestion.close();

					startGame(null, accepteds);
				} else {
					OutputProtocol protocol = prepareBroadCastProtocol();
					if (protocol != null)
						protocol.questionBits(continueGameQuestion.getAcceptedBits(), continueGameQuestion.getRejectedBits());
				}

				return;
			}
		}

		if (startGameQuestion == null)
			return;

		synchronized (startGameQuestion) {
			if (!startGameQuestion.isOpen())
				return;

			if (seat.equals(startGameQuestion.getQuestioner())) {
				startGameCanceled();

				return;
			}

			if (!startGameQuestion.reject(seat))
				return;

			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.questionBits(startGameQuestion.getAcceptedBits(), startGameQuestion.getRejectedBits());

			if (startGameQuestion.allOpined() && startGameQuestion.getAcceptedCount() > 0) {
				Seat questioner = startGameQuestion.getQuestioner();
				List<Seat> accepteds = new ArrayList<>();
				accepteds.add(startGameQuestion.getQuestioner());
				for (int i = 0; i < startGameQuestion.getQuestionedCount(); i++)
					accepteds.add(startGameQuestion.getQuestioned(i));

				startGameQuestion.close();

				startGame(questioner, accepteds);
			} else if (startGameQuestion.allRejected())
				startGameCanceled();
		}
	}

	protected void rejectStopGame(Seat seat) {
		if (stopGameQuestion == null)
			return;

		synchronized (stopGameQuestion) {
			if (!stopGameQuestion.isOpen())
				return;

			if (seat.equals(stopGameQuestion.getQuestioner())) {
				stopGameCanceled();

				return;
			}

			if (!stopGameQuestion.reject(seat))
				return;

			OutputProtocol protocol = preparePlayingBroadCastProtocol(seat);
			if (protocol != null) {
				User user = seat.getUser();
				protocol.stopGameSuggestRejected(user.getName(), user.getID());
			}

			stopGameCanceled();
		}
	}

	@Override
	public boolean removeReconnecting(User user) {
		if (super.removeReconnecting(user))
			return false;

		post(() -> {
			int seatIndex = getSeatIndex(user);
			if (seatIndex != -1)
				standUpInternal(seatIndex, true);

			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.playerReconnectingFailed(user.getName(), user.getID());

			if (getSeatCount() == 0 && watchers.size() == 0)
				close();
			else {
				notifyChanged();
				notifyFocus();
				notifyAvatars();
			}
		});

		return true;
	}

	@Override
	protected boolean removeUserInternal(User user, boolean normalExit) {
		if (!super.removeUserInternal(user, normalExit))
			return false;

		if (!watchers.remove(user))
			synchronized (seats) {
				for (int i = 0; i < seats.length; i++) {
					if (seats[i] == null)
						continue;

					Seat seat = seats[i];
					synchronized (seat) {
						if (user.equals(seat.getUser())) {
							if (normalExit)
								standUpInternal(i, true);

							break;
						}
					}
				}
			}

		logToOut("User " + user.getName() + " has left from the table" + (!normalExit ? " and is trying to reconnecting" : ""));

		OutputProtocol protocol = prepareBroadCastProtocol();
		if (protocol != null)
			if (normalExit)
				protocol.playerDisconnect(user.getName(), user.getID());
			else
				protocol.playerReconnecting(user.getName(), user.getID());

		if (user.equals(host)) {
			host = nextHost();

			if (host != null) {
				protocol = host.prepareProtocol();
				if (protocol != null)
					protocol.youReceivedTheHostFromTheServer();
			}
		}

		if (getSeatCount() == 0 && watchers.size() == 0)
			close();
		else {
			notifyChanged();
			notifyFocus();
			notifyAvatars();
		}

		room.notifyUpdated(user.getName());

		return true;
	}

	protected synchronized void rollBackConfig() {
		privacy = oldPrivacy;
		noWatches = oldNoWatches;
	}

	protected synchronized void saveConfig() {
		oldPrivacy = privacy;
		oldNoWatches = noWatches;
	}

	public List<Seat> seats() {
		return seats(false);
	}

	public List<Seat> seats(boolean includeReconnecting) {
		ArrayList<Seat> result = new ArrayList<>();

		if (includeReconnecting)
			synchronized (seats) {
				for (Seat seat : seats)
					if (seat != null)
						result.add(seat);
			}
		else
			synchronized (seats) {
				for (Seat seat : seats)
					if (seat != null && !seat.getUser().isReconnecting())
						result.add(seat);
			}

		return result;
	}

	public List<Seat> seatsThatCanStart() {
		ArrayList<Seat> result = new ArrayList<>();
		synchronized (seats) {
			for (Seat seat : seats)
				if (seat != null && !seat.getUser().isReconnecting() && seat.canStart())
					result.add(seat);

			return result;
		}
	}

	public final boolean sit(int seatIndex, User user) {
		return send((ReturnableProcess<Boolean>) () -> sitInternal(seatIndex, user));
	}

	protected boolean sitInternal(int seatIndex, User user) {
		synchronized (seats) {
			if (seatIndex < 0 || seatIndex >= seats.length || seats[seatIndex] != null)
				return false;

			if (getSeatIndex(user) != -1)
				return false;

			seats[seatIndex] = createSeat(seatIndex, user);
			watchers.remove(user);

			logToOut("User " + user.getName() + " sit on the seat #" + seatIndex);
			if (DEBUG)
				System.out.println("The user " + user.getName() + " sit in the seat " + seatIndex + " and received the descriptor: " + seats[seatIndex]);

			notifyChanged();
			notifyAvatars();
			notifyFocus();

			return true;
		}
	}

	protected void standUpInternal(int seatIndex, boolean removing) {
		synchronized (seats) {
			Seat seat = seats[seatIndex];
			if (seat == null)
				return;

			synchronized (seat) {
				logToOut("User " + seat.getUser().getName() + " has stand up from the seat #" + seatIndex);

				rejectStartGame(seat);
				rejectStopGame(seat);

				User user = seat.getUser();

				seat.leave();
				seats[seatIndex] = null;

				if (!removing) {
					watchers.add(user);

					OutputProtocol protocol = prepareBroadCastProtocol();
					if (protocol != null)
						protocol.playerStandUp(user.getName(), user.getID());

					notifyChanged();
					notifyAvatars();
					notifyFocus();
				}
			}
		}
	}

	public List<Seat> startedButNotPlayingSeats() {
		synchronized (seats) {
			ArrayList<Seat> result = new ArrayList<>();
			for (Seat seat : seats)
				if (seat != null && seat.canStart() && seat.wantToStart())
					result.add(seat);

			return result;
		}
	}

	public List<Seat> startedSeats() {
		synchronized (seats) {
			ArrayList<Seat> result = new ArrayList<>();
			for (Seat seat : seats)
				if (seat != null) {
					Player player = seat.getPlayer();
					if (player != null && player.isStarted())
						result.add(seat);
				}

			return result;
		}
	}

	protected final void startGame(Seat starter, List<Seat> seats) {
		synchronized (this) {
			if (isClosing() || isClosed())
				return;
		}

		if (game.isRunning())
			return;

		assert startGameQuestion != null && !startGameQuestion.isOpen();

		synchronized (seats) {
			for (Seat seat : seats)
				if (!seat.start())
					onSeatCantStart(seat);
		}

		synchronized (game) {
			if (game.getStartedCount() > 1) {
				if (!game.start())
					onStartGameCanceled(starter, NO_REASON);
			} else
				onStartGameCanceled(starter, NO_ENOUGH_PLAYERS);
		}
	}

	private void startGameCanceled() {
		int count;
		Seat questioned;
		Seat questioner;
		synchronized (startGameQuestion) {
			count = startGameQuestion.getQuestionedCount();
			questioned = startGameQuestion.getQuestioned(0);
			questioner = startGameQuestion.getQuestioner();

			startGameQuestion.close();
		}

		if (questioned != null) {
			User user = questioned.getUser();
			if (count == 1 && user != null) {
				OutputProtocol protocol = user.prepareProtocol();
				if (protocol != null)
					protocol.startGameSuggestRejected(user.getName(), user.getID());
			}
		}

		onStartGameCanceled(questioner, NO_REASON);
	}

	private void stopGameCanceled() {
		if (stopGameQuestion != null)
			synchronized (stopGameQuestion) {
				if (!stopGameQuestion.isOpen())
					return;

				stopGameQuestion.close();

				OutputProtocol protocol = prepareBroadCastProtocol();
				if (protocol != null)
					protocol.questionCanceled();
			}
	}

	protected void suggestStartGame(Seat seat) {
		synchronized (this) {
			if (isClosing() || isClosed())
				return;
		}

		if (continueGameQuestion == null)
			return;

		synchronized (continueGameQuestion) {
			if (continueGameQuestion.isOpen()) {
				acceptStartGame(seat);
				return;
			}
		}

		synchronized (startGameQuestion) {
			if (startGameQuestion.isOpen()) {
				acceptStartGame(seat);

				return;
			}

			if (!seat.canStart())
				return;

			for (Seat seat1 : seats)
				if (seat1 != null)
					seat1.wantToStart = false;

			seat.wantToStart = true;

			List<Seat> questioneds = seatsThatCanStart();
			notifySeatsWithSameIP(questioneds);
			questioneds.remove(seat);
			if (questioneds.size() == 0) {
				onStartGameCanceled(seat, NO_ENOUGH_PLAYERS);

				return;
			}

			User user = seat.getUser();
			startGameQuestion.open(seat, questioneds, (question, accepteds) -> onStartGameTimeOut(question, accepteds));

			OutputProtocol protocol = prepareBroadCastProtocol(user);
			if (protocol != null)
				protocol.questionBits(1 << seat.getIndex(), 0);
			protocol = prepareBroadCastProtocol(user);
			if (protocol != null)
				protocol.startGameSuggested(user.getName(), user.getID(), true);
			protocol = prepareSeatBroadCastProtocol(questioneds, seat);
			if (protocol != null)
				protocol.startGameSuggested(user.getName(), user.getID(), false);
		}
	}

	protected void suggestStopGame(Seat seat) {
		synchronized (this) {
			if (isClosing() || isClosed())
				return;
		}

		synchronized (stopGameQuestion) {
			if (stopGameQuestion.isOpen()) {
				acceptStopGame(seat);

				return;
			}

			List<Seat> questioneds = playingSeats();
			questioneds.remove(seat);
			User user = seat.getUser();
			stopGameQuestion.open(seat, questioneds, (question, accepteds) -> onStopGameTimeOut(question, accepteds));
			OutputProtocol protocol = prepareBroadCastProtocol(user);
			if (protocol != null)
				protocol.questionBits(1 << seat.getIndex(), 0);
			protocol = prepareSeatBroadCastProtocol(questioneds);
			if (protocol != null)
				protocol.stopGameSuggested(user.getName(), user.getID());
		}
	}

	@Override
	public int tableCount(String user) {
		return room.tableCount(user);
	}

	@Override
	public List<Table> tables(String user) {
		return room.tables(user);
	}

	@Override
	public String toString() {
		return "table " + number + (room != null ? " in room " + room.getName() : "");
	}

	protected boolean validatePrivacy(int privacy) {
		return privacy == PUBLIC || privacy == PRIVATE;
	}

	public boolean wasBooted(String name) {
		return booteds.contains(name);
	}

	public boolean wasInvited(String name) {
		return getInviteQuestion(name) != null;
	}

	public synchronized void write(JCheckersDataOutputStream output) {
		output.writeInt(id);
		output.writeInt(number);
		if (host != null) {
			output.writeBoolean(true);
			output.writeString(host.getName());
			output.writeInt(host.getID());
		} else {
			output.writeBoolean(false);
			output.writeShort(0);
			output.writeInt(0);
		}
		output.writeChar(getSeatBits());
		output.writeChar(getReconnectBits());
		output.writeChar(getSeatCount());
		for (int i = 0; i < seats.length; i++) {
			if (seats[i] == null)
				continue;

			seats[i].write(output);
		}
		output.writeInt(watchers.size());
		for (User user : watchers)
			writeUser(user, output);
		output.writeChar(getGameID());
		writeConfig(output);
	}

	protected synchronized void writeConfig(JCheckersDataOutputStream output) {
		output.writeChar(0);
		output.writeInt(privacy);
		output.writeBoolean(true);
		output.writeInt(game.getTimePerTurn());
		output.writeBoolean(noWatches);
	}

	@Override
	public void writeUser(User user, JCheckersDataOutputStream output) {
		output.writeString(user.getName());
		output.writeInt(user.getID());
	}

}
