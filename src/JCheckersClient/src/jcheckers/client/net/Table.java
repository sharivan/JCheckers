package jcheckers.client.net;

import common.util.BitUtil;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class Table {

	public class Seat {

		private User user;
		private int reconnectingTime;

		protected Seat(JCheckersDataInputStream in) throws JCheckersIOException {
			user = connection.createUser();
			readUser(user, in);
			reconnectingTime = in.readInt();
		}

		public int getReconnectingTime() {
			return reconnectingTime;
		}

		public User getUser() {
			return user;
		}

	}

	private Connection connection;
	private int roomID;
	private int id;
	private int number;

	private boolean hasHost;
	private String hostName;
	private int hostID;

	private Seat[] seats;
	private int seatBits;
	private int reconnectingBits;

	private User[] watchers;

	private TableParams params;

	protected Table(Connection connection, int roomID, int id, JCheckersDataInputStream in) throws JCheckersIOException {
		this.connection = connection;
		this.roomID = roomID;
		this.id = id;

		readFrom(in);
	}

	protected abstract TableParams createParams(JCheckersDataInputStream in) throws JCheckersIOException;

	public int getGameID() {
		return params.getGameID();
	}

	public User getHost() {
		if (hostID == 0)
			return null;

		for (Seat seat : seats) {
			if (seat == null)
				continue;

			User user = seat.getUser();
			if (user.getID() == hostID)
				return user;
		}

		for (User watcher : watchers)
			if (watcher.getID() == hostID)
				return watcher;

		return null;
	}

	public int getHostID() {
		return hostID;
	}

	public String getHostName() {
		return hostName;
	}

	public int getID() {
		return id;
	}

	public abstract int getMaxSeatCount();

	public int getNumber() {
		return number;
	}

	public TableParams getParams() {
		return params;
	}

	public int getReconnectingBits() {
		return reconnectingBits;
	}

	public int getRoomID() {
		return roomID;
	}

	public Seat getSeat(int index) {
		return seats[index];
	}

	public int getSeatBits() {
		return seatBits;
	}

	public int getSeatCount() {
		return BitUtil.bitCount(seatBits);
	}

	public User getUserByID(int id) {
		for (Seat seat : seats)
			if (seat != null) {
				User user = seat.getUser();
				if (seat.getUser().getID() == id)
					return user;
			}

		for (User user : watchers)
			if (user.getID() == id)
				return user;

		return null;
	}

	public User getWatcher(int index) {
		return watchers[index];
	}

	public int getWatcherCount() {
		return watchers.length;
	}

	public boolean hasHost() {
		return hasHost;
	}

	public void readFrom(JCheckersDataInputStream in) throws JCheckersIOException {
		number = in.readInt();
		hasHost = in.readBoolean();
		hostName = in.readString();
		hostID = in.readInt();
		seatBits = in.readUChar();
		reconnectingBits = in.readUChar();

		@SuppressWarnings("unused")
		int seatCount = in.readUChar();
		seats = new Seat[getMaxSeatCount()];
		Iterable<Integer> iterable = BitUtil.bitIterable(seatBits);
		for (int seatIndex : iterable)
			seats[seatIndex] = new Seat(in);

		int watchersCount = in.readInt();
		watchers = new User[watchersCount];
		for (int i = 0; i < watchersCount; i++) {
			User user = connection.createUser();
			readUser(user, in);
			watchers[i] = user;
		}

		params = createParams(in);
	}

	protected void readUser(User user, JCheckersDataInputStream in) throws JCheckersIOException {
		user.name = in.readString();
		user.id = in.readInt();

		user.readExtraInfo(in);
	}

}
