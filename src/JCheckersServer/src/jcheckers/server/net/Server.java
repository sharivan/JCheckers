package jcheckers.server.net;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import common.config.Config;
import common.config.ConfigEntry;
import common.config.MapConfigEntry;
import common.db.SQLCommand;
import common.db.SQLConnection;
import common.db.SQLConnectionPool;
import common.db.SQLExpression;
import common.db.SQLFormater;
import common.db.Transaction;
import common.io.Log;
import common.process.Closer;
import common.process.Interruptable;
import common.process.NonReturnableProcessWithoutArg;
import common.process.ProcessQueue;
import common.process.ReturnableProcess;
import common.process.TimeOutException;
import common.process.timer.Timer;
import common.util.DateTimeUtil;
import common.util.Tree;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.logic.Game.StopReason;
import jcheckers.server.io.DuplicateOpcodeException;
import jcheckers.server.io.InputProtocol;
import jcheckers.server.io.OutputProtocol;

public abstract class Server implements Interruptable {

	public static enum BanRestriction {
		CHAT, ACCESS
	}

	public static enum BanType {
		NICK, COMPID, IP
	}

	private static <T extends Enum<T>> T asEnum(Class<T> enumType, String name) {
		return Enum.valueOf(enumType, name.trim());
	}

	private static <T extends Enum<T>> Set<T> asSet(Class<T> enumType, String value) {
		Set<T> result = EnumSet.noneOf(enumType);
		value = value.trim();
		if (value.equals(""))
			return result;

		String[] values = value.split(",");
		for (String name : values) {
			T e = asEnum(enumType, name);
			result.add(e);
		}

		return result;
	}

	private String host;
	private int port;
	private String title;

	private Closer closer;
	private int objectsToClose;
	private int objectsClosed;

	private String homeDir;
	private String logDir;
	private Log log;

	private ServerSocket server;
	private ThreadGroup group;
	private ProcessQueue queue;
	private ProcessQueue destroyerQueue;
	private ProcessQueue acceptorQueue;
	private SQLConnectionPool pool;
	protected Vector<Connection> connections;
	Hashtable<String, List<Connection>> names;
	private Vector<Room> rooms;
	private Timer tmrNotifyRoomList;
	private Timer tmbUpdateDB;
	private Vector<Room> updatedRooms;

	protected Server() {

	}

	private void accept() {
		if (!closer.isOpen())
			return;

		try {
			Socket socket = server.accept();
			socket.setSoLinger(false, 1);
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(Connection.CHECK_CONNECTION_INTERVAL);
			synchronized (connections) {
				Connection connection = new Connection(this, socket);
				connections.add(connection);
			}
		} catch (SocketException e) {
		} catch (IOException e) {
			handleIOException(e);
		} catch (Throwable e) {
			handleException(e);
		}
	}

	private boolean addBan(int room, User user, long time, Set<BanType> type, BanRestriction restriction, String reason) {
		return addBan(room, user, time, type, restriction, null, reason);
	}

	private boolean addBan(int room, User user, long time, Set<BanType> type, BanRestriction restriction, String admin, String reason) {
		return send(() -> {
			InetAddress ip = user.getIP();
			try {
				Timestamp when = new Timestamp(System.currentTimeMillis());
				boolean result = execute(SQLCommand.insert(DBConsts.TABLE_BANS, true,
						new Object[] { null, getGameName(), room, user.getName(), user.getCompID(), ip != null ? ip.getHostAddress() : null, type, restriction, admin, when, time, reason }));

				OutputProtocol protocol = user.prepareProtocol();
				if (protocol != null)
					if (restriction == BanRestriction.CHAT)
						protocol.adminChat("SYSTEM", "###3" + (time != -1 ? " " + time / 60000 : ""));
					else if (restriction == BanRestriction.ACCESS) {
						Calendar calendar = Calendar.getInstance();
						long banExpires = time != -1 ? when.getTime() + time : -1;
						if (banExpires != -1)
							calendar.setTimeInMillis(banExpires);
						protocol.notifyBannedAndDisconnect(banExpires != -1 ? new SimpleDateFormat("dd/MM/yyyy HH:mm z").format(calendar.getTime()) : "");
						user.close();
					}

				return result;
			} catch (SQLException e) {
				logToErr(e);
			} catch (InterruptedException e) {
				close();
			}

			return false;
		});
	}

	private boolean addBan(User user, long time, Set<BanType> type, BanRestriction restriction, String reason) {
		return addBan(-1, user, time, type, restriction, null, reason);
	}

	private boolean addBan(User user, long time, Set<BanType> type, BanRestriction restriction, String admin, String reason) {
		return addBan(-1, user, time, type, restriction, admin, reason);
	}

	private boolean addBan(User user, Set<BanType> type, BanRestriction restriction, String reason) {
		return addBan(-1, user, -1, type, restriction, null, reason);
	}

	private boolean ban(int room, User user, long time, Set<BanType> type, String reason) {
		return addBan(room, user, time, type, BanRestriction.ACCESS, reason);
	}

	private boolean ban(int room, User user, long time, Set<BanType> type, String admin, String reason) {
		return addBan(room, user, time, type, BanRestriction.ACCESS, admin, reason);
	}

	public boolean ban(int room, User user, long time, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), reason);
	}

	public boolean ban(int room, User user, long time, String admin, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), admin, reason);
	}

	private boolean ban(User user, long time, Set<BanType> type, String reason) {
		return addBan(user, time, type, BanRestriction.ACCESS, reason);
	}

	private boolean ban(User user, long time, Set<BanType> type, String admin, String reason) {
		return addBan(user, time, type, BanRestriction.ACCESS, admin, reason);
	}

	public boolean ban(User user, long time, String reason) {
		return ban(user, time, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), reason);
	}

	public boolean ban(User user, long time, String admin, String reason) {
		return ban(user, time, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), admin, reason);
	}

	private boolean ban(User user, Set<BanType> type, String reason) {
		return addBan(user, type, BanRestriction.ACCESS, reason);
	}

	public boolean ban(User user, String reason) {
		return ban(user, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), reason);
	}

	public boolean banByCompid(int room, User user, long time, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.COMPID), reason);
	}

	public boolean banByCompid(int room, User user, long time, String admin, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.COMPID), admin, reason);
	}

	public boolean banByCompid(User user, long time, String reason) {
		return ban(user, time, EnumSet.of(BanType.COMPID), reason);
	}

	public boolean banByCompid(User user, long time, String admin, String reason) {
		return ban(user, time, EnumSet.of(BanType.COMPID), admin, reason);
	}

	public boolean banByCompid(User user, String reason) {
		return ban(user, EnumSet.of(BanType.COMPID), reason);
	}

	public boolean banByCompidAndIP(int room, User user, long time, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.COMPID, BanType.IP), reason);
	}

	public boolean banByCompidAndIP(int room, User user, long time, String admin, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.COMPID, BanType.IP), admin, reason);
	}

	public boolean banByCompidAndIP(User user, long time, String reason) {
		return ban(user, time, EnumSet.of(BanType.COMPID, BanType.IP), reason);
	}

	public boolean banByCompidAndIP(User user, long time, String admin, String reason) {
		return ban(user, time, EnumSet.of(BanType.COMPID, BanType.IP), admin, reason);
	}

	public boolean banByCompidAndIP(User user, String reason) {
		return ban(user, EnumSet.of(BanType.COMPID, BanType.IP), reason);
	}

	public boolean banByIP(int room, User user, long time, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.IP), reason);
	}

	public boolean banByIP(int room, User user, long time, String admin, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.IP), admin, reason);
	}

	public boolean banByIP(User user, long time, String reason) {
		return ban(user, time, EnumSet.of(BanType.IP), reason);
	}

	public boolean banByIP(User user, long time, String admin, String reason) {
		return ban(user, time, EnumSet.of(BanType.IP), admin, reason);
	}

	public boolean banByIP(User user, String reason) {
		return ban(user, EnumSet.of(BanType.IP), reason);
	}

	public boolean banByName(int room, User user, long time, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.NICK), reason);
	}

	public boolean banByName(int room, User user, long time, String admin, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.NICK), admin, reason);
	}

	public boolean banByName(User user, long time, String reason) {
		return ban(user, time, EnumSet.of(BanType.NICK), reason);
	}

	public boolean banByName(User user, long time, String admin, String reason) {
		return ban(user, time, EnumSet.of(BanType.NICK), admin, reason);
	}

	public boolean banByName(User user, String reason) {
		return ban(user, EnumSet.of(BanType.NICK), reason);
	}

	public boolean banByNameAndCompid(int room, User user, long time, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.NICK, BanType.COMPID), reason);
	}

	public boolean banByNameAndCompid(int room, User user, long time, String admin, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.NICK, BanType.COMPID), admin, reason);
	}

	public boolean banByNameAndCompid(User user, long time, String reason) {
		return ban(user, time, EnumSet.of(BanType.NICK, BanType.COMPID), reason);
	}

	public boolean banByNameAndCompid(User user, long time, String admin, String reason) {
		return ban(user, time, EnumSet.of(BanType.NICK, BanType.COMPID), admin, reason);
	}

	public boolean banByNameAndCompid(User user, String reason) {
		return ban(user, EnumSet.of(BanType.NICK, BanType.COMPID), reason);
	}

	public boolean banByNameAndIP(int room, User user, long time, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.NICK, BanType.IP), reason);
	}

	public boolean banByNameAndIP(int room, User user, long time, String admin, String reason) {
		return ban(room, user, time, EnumSet.of(BanType.NICK, BanType.IP), admin, reason);
	}

	public boolean banByNameAndIP(User user, long time, String reason) {
		return ban(user, time, EnumSet.of(BanType.NICK, BanType.IP), reason);
	}

	public boolean banByNameAndIP(User user, long time, String admin, String reason) {
		return ban(user, time, EnumSet.of(BanType.NICK, BanType.IP), admin, reason);
	}

	public boolean banByNameAndIP(User user, String reason) {
		return ban(user, EnumSet.of(BanType.NICK, BanType.IP), reason);
	}

	private void checkClosed() {
		synchronized (this) {
			objectsClosed++;
		}

		logToOut("Destroyer queue closed.");

		destroyObjects();

		if (pool != null) {
			pool.destroy();
			pool = null;
		}

		connections.clear();
		names.clear();
		updatedRooms.clear();
		rooms.clear();

		synchronized (log) {
			if (!log.isClosed()) {
				log.logToOut("Server log closed.");
				log.logToErr("Server error log closed.");
				log.close();
			}
		}

		closer.stopClosing();
	}

	@Override
	public void close() {
		close(closer);
	}

	public void close(boolean wait) throws TimeOutException {
		close(wait, Closer.DEFAULT_CLOSE_TIMEOUT, false);
	}

	public void close(boolean wait, boolean force) throws TimeOutException {
		close(wait, Closer.DEFAULT_CLOSE_TIMEOUT, force);
	}

	public void close(boolean wait, int timeout) throws TimeOutException {
		close(wait, timeout, false);
	}

	public void close(boolean wait, int timeout, boolean force) throws TimeOutException {
		close(closer);
		logToOut("Waiting to close destroyer queue...");
		while (true) {
			int old = objectsClosed;
			try {
				destroyerQueue.waitForClose(timeout);

				break;
			} catch (TimeOutException e) {
				if (objectsClosed <= old) {
					logToErr("WARNING: Timeout to close destroyer queue.");
					if (force) {
						logToOut("Destroyer queue interrupted.");
						interrupt();
					}

					throw e;
				}
				old = objectsClosed;
			}
		}
	}

	public void close(Closer closer) {
		try {
			close(closer, false, Closer.DEFAULT_CLOSE_TIMEOUT, false);
		} catch (TimeOutException e) {
		}
	}

	public void close(Closer closer, boolean wait) throws TimeOutException {
		close(closer, wait, Closer.DEFAULT_CLOSE_TIMEOUT, false);
	}

	public void close(Closer closer, boolean wait, boolean force) throws TimeOutException {
		close(closer, wait, Closer.DEFAULT_CLOSE_TIMEOUT, force);
	}

	public void close(Closer closer, boolean wait, int timeout) throws TimeOutException {
		close(closer, wait, timeout, false);
	}

	public void close(Closer closer, boolean wait, int timeout, boolean force) throws TimeOutException {
		try {
			if (isRunningInDestroyerQueue())
				closer.close(wait, timeout, force);
			else if (wait) {
				TimeOutException exception = destroyerQueue.postAndWait(() -> {
					try {
						closer.close(wait, timeout, force);
					} catch (TimeOutException e) {
						return e;
					}

					return null;
				});
				if (exception != null)
					throw exception;
			} else
				destroyerQueue.post(() -> closer.close());
		} catch (InterruptedException e) {
		}
	}

	private void closeInternal() {
		List<Connection> connections;
		synchronized (this.connections) {
			for (Connection connection : this.connections)
				objectsToClose += connection.closingMaxScore();

			connections = connections();
		}

		List<Room> rooms;
		synchronized (this.connections) {
			for (Room room : this.rooms)
				objectsToClose += room.closingMaxScore();

			rooms = rooms();
		}

		for (Room room : rooms)
			room.stopAllGames(StopReason.CANCELED);

		destroyObjects();

		objectsClosed = 0;

		logToOut("Closing acceptor queue...");
		try {
			acceptorQueue.close(true, true);
		} catch (TimeOutException e) {
			logToErr("WARNING: The server acceptor queue was forced closed.");
		} finally {
			logToOut("Closing acceptor closed.");
			objectsClosed++;
		}

		for (Connection connection : connections) {
			int oldObjectsClosed = objectsClosed;
			logToOut("Closing " + connection + "...");
			while (true) {
				int oldScore = 0;
				try {
					connection.close(true);

					break;
				} catch (TimeOutException e) {
					int score = connection.closingScore();
					if (score <= oldScore) {
						connection.interrupt();
						logToErr("WARNING: The " + connection + " was forced closed.");

						break;
					}
					oldScore = score;
				} finally {
					objectsClosed = oldObjectsClosed + connection.closingScore();
				}
			}
			logToOut(connection + " closed.");
		}

		for (Room room : rooms) {
			int oldObjectsClosed = objectsClosed;
			logToOut("Closing " + room + " ...");
			while (true) {
				int oldScore = 0;
				try {
					room.close(true);

					break;
				} catch (TimeOutException e) {
					int score = room.closingScore();
					if (score <= oldScore) {
						room.interrupt();
						logToErr("WARNING: The " + room + " was forced closed.");

						break;
					}
					oldScore = score;
				} finally {
					objectsClosed = oldObjectsClosed + room.closingScore();
				}
			}
			logToOut(room + " closed.");
		}

		logToOut("Closing queue...");
		try {
			queue.close(true);
			logToOut("Queue closed.");
		} catch (TimeOutException e) {
			queue.interrupt();
			logToOut("Queue interrupted.");
			logToErr("WARNING: The server queue was forced closed.");
		} finally {
			objectsClosed++;
		}

		destroyerQueue.close();
	}

	public int closingMaxScore() {
		return objectsToClose;
	}

	public int closingScore() {
		return objectsClosed;
	}

	public List<Connection> connections() {
		synchronized (connections) {
			return new ArrayList<>(connections);
		}
	}

	public InputProtocol createInputProtocol(JCheckersDataInputStream input) {
		return new InputProtocol(input);
	}

	private void createLogs(String logDir) throws IOException {
		File home = new File(homeDir);
		File file = new File(logDir);
		if (!file.isAbsolute()) {
			file = new File(home, logDir);
			logDir = file.getAbsolutePath();
		}

		if (!file.exists() && !file.mkdirs())
			throw new IOException("Could not create the server log directory " + logDir);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		String s = DateTimeUtil.dateToStr_(calendar);
		log = new Log(new File(logDir, "server[" + s + "].txt"), new File(logDir, "errors[" + s + "].txt"));

		log.logToOut("Server log started for the game " + getGameName());
		log.logToErr("Server error log started for the game " + getGameName());
	}

	public OutputProtocol createOutputProtocol(JCheckersDataOutputStream output) {
		return new OutputProtocol(output);
	}

	private void destroyObjects() {
		if (tmrNotifyRoomList != null) {
			tmrNotifyRoomList.close();
			tmrNotifyRoomList = null;
		}

		if (tmbUpdateDB != null) {
			tmbUpdateDB.close();
			tmbUpdateDB = null;
		}

		if (server != null)
			try {
				server.close();
			} catch (IOException e) {
			}
	}

	public boolean execute(String command) throws SQLException, InterruptedException {
		return pool != null ? pool.execute(command) : false;
	}

	public ResultSet execute2(String command) throws SQLException, InterruptedException {
		if (pool == null)
			throw new SQLException("Pool is null");

		return pool.execute2(command);
	}

	public ResultSet executeQuery(String command) throws SQLException, InterruptedException {
		if (pool == null)
			throw new SQLException("Pool is null");

		return pool.executeQuery(command);
	}

	public ResultSet executeQuery(String command, int resultSetType, int resultSetConcurrency) throws SQLException, InterruptedException {
		if (pool == null)
			throw new SQLException("Pool is null");

		return pool.executeQuery(command, resultSetType, resultSetConcurrency);
	}

	public <T> T executeStatsTransaction(String user, Transaction<T> transaction) throws SQLException, InterruptedException {
		return pool != null ? pool.executeTransaction((connection) -> {
			connection.lock(tableStats(), user);
			try {
				return transaction.execute(connection);
			} finally {
				connection.unlock(tableStats(), user);
			}
		}) : null;
	}

	public <T> T executeTransaction(Transaction<T> transaction) throws SQLException, InterruptedException {
		return pool != null ? pool.executeTransaction(transaction) : null;
	}

	public int executeUpdate(String command) throws SQLException, InterruptedException {
		return pool != null ? pool.executeUpdate(command) : 0;
	}

	public <T> T executeUserTransaction(String user, Transaction<T> transaction) throws SQLException, InterruptedException {
		return pool != null ? pool.executeTransaction((connection) -> {
			connection.lock(DBConsts.TABLE_USERS, user);
			try {
				return transaction.execute(connection);
			} finally {
				connection.unlock(DBConsts.TABLE_USERS, user);
			}
		}) : null;
	}

	public int getAdminLevel(int room, String user) {
		if (isSystem(user) || isAdminMaster(user))
			return 6;

		int level = getServerAdminLevel(user);
		if (level >= 0)
			return level;

		return getLobbyAdminLevel(room, user);
	}

	private long getBanExpires(int room, String typeName, Object typeValue, BanType type, BanRestriction restriction) {
		long result = 0;
		long max = 0;
		try (ResultSet rs = executeQuery(SQLCommand.selectAll(DBConsts.TABLE_BANS, new String[] { typeName }, new Object[] { typeValue }))) {
			long now = System.currentTimeMillis();
			while (rs.next()) {
				String game = rs.getString("game");
				if (game != null && !game.equals(getGameName()))
					continue;

				if (room != -1) {
					int room1 = rs.getInt("room");
					if (room1 != -1 && room1 != room)
						continue;
				}

				Set<BanType> type1 = asSet(BanType.class, rs.getString("type"));
				if (!type1.contains(type))
					continue;

				BanRestriction restriction1 = asEnum(BanRestriction.class, rs.getString("restriction"));
				if (!restriction1.equals(restriction))
					continue;

				Timestamp when = rs.getTimestamp("when");
				if (when == null)
					return -1;
				long whenMillis = when.getTime();
				if (whenMillis > now)
					continue;

				long duration = rs.getLong("duration");
				if (duration == -1)
					return -1;
				long t = whenMillis + duration - now;
				if (t > max) {
					max = t;
					result = t + now;
				}

				// TODO Deve ser removido o ban caso ele já tenha sido expirado
			}
		} catch (SQLException e) {
			handleException(e);
		} catch (InterruptedException e) {
			close();
		}

		return result;
	}

	public long getBanExpiresByCompid(int room, int compid) {
		return getBanExpires(room, "compid", compid, BanType.COMPID, BanRestriction.ACCESS);
	}

	public long getBanExpiresByCompid(int room, User user) {
		return getBanExpiresByCompid(room, user.getCompID());
	}

	public long getBanExpiresByCompidOrIP(int room, int compid, String ip) {
		long expires = getBanExpiresByCompid(room, compid);
		if (expires != 0)
			return expires;
		return getBanExpiresByIP(room, ip);
	}

	public long getBanExpiresByCompidOrIP(int room, User user) {
		InetAddress addr = user.getIP();
		return getBanExpiresByCompidOrIP(room, user.getCompID(), addr != null ? addr.getHostAddress() : null);
	}

	public long getBanExpiresByIP(int room, String ip) {
		return getBanExpires(room, "ip", ip, BanType.IP, BanRestriction.ACCESS);
	}

	public long getBanExpiresByIP(int room, User user) {
		InetAddress addr = user.getIP();
		return getBanExpiresByIP(room, addr != null ? addr.getHostAddress() : null);
	}

	public long getBanExpiresByName(int room, String name) {
		return getBanExpires(room, "user", name, BanType.NICK, BanRestriction.ACCESS);
	}

	public long getBanExpiresByName(int room, User user) {
		return getBanExpiresByName(room, user.getName());
	}

	public long getBanExpiresByNameOrCompid(int room, String name, int compid) {
		long expires = getBanExpiresByName(room, name);
		if (expires != 0)
			return expires;
		return getBanExpiresByCompid(room, compid);
	}

	public long getBanExpiresByNameOrCompid(int room, User user) {
		return getBanExpiresByNameOrCompid(room, user.getName(), user.getCompID());
	}

	public long getBanExpiresByNameOrCompidOrIP(int room, String name, int compid, String ip) {
		long expires = getBanExpiresByName(room, name);
		if (expires != 0)
			return expires;
		expires = getBanExpiresByCompid(room, compid);
		if (expires != 0)
			return expires;
		return getBanExpiresByIP(room, ip);
	}

	public long getBanExpiresByNameOrCompidOrIP(int room, User user) {
		InetAddress addr = user.getIP();
		return getBanExpiresByNameOrCompidOrIP(room, user.getName(), user.getCompID(), addr != null ? addr.getHostAddress() : null);
	}

	public long getBanExpiresByNameOrIP(int room, String name, String ip) {
		long expires = getBanExpiresByName(room, name);
		if (expires != 0)
			return expires;
		return getBanExpiresByIP(room, ip);
	}

	public long getBanExpiresByNameOrIP(int room, User user) {
		InetAddress addr = user.getIP();
		return getBanExpiresByNameOrIP(room, user.getName(), addr != null ? addr.getHostAddress() : null);
	}

	public SQLConnection getConnection() throws InterruptedException, SQLException {
		return pool != null ? pool.getConnection() : null;
	}

	public SQLConnection getConnection(boolean releaseOnCompletion, boolean autoCommit) throws InterruptedException, SQLException {
		return pool != null ? pool.getConnection(releaseOnCompletion, autoCommit) : null;
	}

	public abstract int getGameID();

	public abstract String getGameName();

	protected ThreadGroup getGroup() {
		return group;
	}

	public String getHomeDir() {
		return homeDir;
	}

	public String getHost() {
		return host;
	}

	public abstract String getLastRelease();

	public int getLobbyAdminLevel(int room, String user) {
		try (ResultSet rs = executeQuery(SQLCommand.selectAll(DBConsts.TABLE_ADMINS, new String[] { "game", "room", "name" }, new Object[] { getGameName(), room, user }))) {
			if (rs.next())
				return rs.getInt("level");
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}

		return -1;
	}

	public Room getLobbyByID(int id) {
		synchronized (rooms) {
			for (Room lobby : rooms)
				if (lobby.getID() == id)
					return lobby;

			return null;
		}
	}

	public Room getLobbyByIndex(int index) {
		synchronized (rooms) {
			for (Room lobby : rooms)
				if (lobby.getIndex() == index)
					return lobby;

			return null;
		}
	}

	public Log getLog() {
		return log;
	}

	public String getLogDir() {
		return logDir;
	}

	public long getMuteExpiresByCompid(int room, int compid) {
		return getBanExpires(room, "compid", compid, BanType.COMPID, BanRestriction.CHAT);
	}

	public long getMuteExpiresByCompid(int room, User user) {
		return getMuteExpiresByCompid(room, user.getCompID());
	}

	public long getMuteExpiresByCompidOrIP(int room, int compid, String ip) {
		long expires = getMuteExpiresByCompid(room, compid);
		if (expires != 0)
			return expires;
		return getMuteExpiresByIP(room, ip);
	}

	public long getMuteExpiresByCompidOrIP(int room, User user) {
		InetAddress addr = user.getIP();
		return getMuteExpiresByCompidOrIP(room, user.getCompID(), addr != null ? addr.getHostAddress() : null);
	}

	public long getMuteExpiresByIP(int room, String ip) {
		return getBanExpires(room, "ip", ip, BanType.IP, BanRestriction.CHAT);
	}

	public long getMuteExpiresByIP(int room, User user) {
		InetAddress addr = user.getIP();
		return getMuteExpiresByIP(room, addr != null ? addr.getHostAddress() : null);
	}

	public long getMuteExpiresByName(int room, String name) {
		return getBanExpires(room, "user", name, BanType.NICK, BanRestriction.CHAT);
	}

	public long getMuteExpiresByName(int room, User user) {
		return getMuteExpiresByName(room, user.getName());
	}

	public long getMuteExpiresByNameOrCompid(int room, String name, int compid) {
		long expires = getMuteExpiresByName(room, name);
		if (expires != 0)
			return expires;
		return getMuteExpiresByCompid(room, compid);
	}

	public long getMuteExpiresByNameOrCompid(int room, User user) {
		return getMuteExpiresByNameOrCompid(room, user.getName(), user.getCompID());
	}

	public long getMuteExpiresByNameOrCompidOrIP(int room, String name, int compid, String ip) {
		long expires = getMuteExpiresByName(room, name);
		if (expires != 0)
			return expires;
		expires = getMuteExpiresByCompid(room, compid);
		if (expires != 0)
			return expires;
		return getMuteExpiresByIP(room, ip);
	}

	public long getMuteExpiresByNameOrCompidOrIP(int room, User user) {
		InetAddress addr = user.getIP();
		return getMuteExpiresByNameOrCompidOrIP(room, user.getName(), user.getCompID(), addr != null ? addr.getHostAddress() : null);
	}

	public long getMuteExpiresByNameOrIP(int room, String name, String ip) {
		long expires = getMuteExpiresByName(room, name);
		if (expires != 0)
			return expires;
		return getMuteExpiresByIP(room, ip);
	}

	public long getMuteExpiresByNameOrIP(int room, User user) {
		InetAddress addr = user.getIP();
		return getMuteExpiresByNameOrIP(room, user.getName(), addr != null ? addr.getHostAddress() : null);
	}

	public int getPort() {
		return port;
	}

	public ProcessQueue getQueue() {
		return queue;
	}

	protected abstract Class<? extends Room> getRoomClass();

	public int getServerAdminLevel(String user) {
		try (ResultSet rs = executeQuery(SQLCommand.selectAll(DBConsts.TABLE_ADMINS, new String[] { "game", "room", "name" }, new Object[] { getGameName(), -1, user }))) {
			if (rs.next())
				return rs.getInt("level");
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}

		return -1;
	}

	protected abstract Class<? extends Table> getTableClass();

	public String getTitle() {
		return title;
	}

	protected abstract Class<? extends User> getUserClass();

	public int getUserCount() {
		return names.size();
	}

	public List<User> getUsersByName(String name) {
		List<User> result = new ArrayList<>();

		List<Room> rooms = rooms();
		for (Room room : rooms) {
			User user = room.getUserByName(name);
			if (user != null)
				result.add(user);

			List<Table> tables = room.tables(name);
			for (Table table : tables) {
				user = table.getUserByName(name);
				if (user != null)
					result.add(user);
			}
		}

		return result;
	}

	public abstract String getVersion();

	protected void handleException(Throwable e) {
		if (e instanceof IOException)
			handleIOException((IOException) e);
		else
			logToErr(e);
	}

	protected void handleIOException(IOException e) {
		close();

		if (e instanceof EOFException)
			return;

		if (e instanceof SocketTimeoutException)
			return;

		logToErr(e);
	}

	public void incrementAbandoneds(String user) {
		try {
			executeStatsTransaction(user, (connection) -> {
				return connection.update(tableStats(), new String[] { "game", "user" }, new Object[] { getGameName(), user }, new String[] { "abandoneds" },
						new Object[] { new SQLExpression("abandoneds + 1") });
			});
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}
	}

	public void incrementLosses(String user, boolean abandoned) {
		try {
			executeStatsTransaction(user, (connection) -> {
				return connection.update(tableStats(), new String[] { "game", "user" }, new Object[] { getGameName(), user }, new String[] { "playeds", "losses", "abandoneds" },
						new Object[] { new SQLExpression("playeds + " + (abandoned ? "0" : "1")), new SQLExpression("losses + 1"), new SQLExpression("abandoneds + " + (abandoned ? "1" : "0")) });
			});
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}
	}

	public void incrementPlayeds(String user) {
		try {
			executeStatsTransaction(user, (connection) -> {
				return connection.update(tableStats(), new String[] { "game", "user" }, new Object[] { getGameName(), user }, new String[] { "playeds" },
						new Object[] { new SQLExpression("playeds + 1") });
			});
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}
	}

	public void incrementWins(String user) {
		try {
			executeStatsTransaction(user, (connection) -> {
				return connection.update(tableStats(), new String[] { "game", "user" }, new Object[] { getGameName(), user }, new String[] { "playeds", "wins" },
						new Object[] { new SQLExpression("playeds + 1"), new SQLExpression("wins + 1") });
			});
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}
	}

	@Override
	public void interrupt() {
		try {
			server.close();
		} catch (IOException e) {
		}

		group.interrupt();

		logToErr("WARNING: All threads were interrupted.");
	}

	public boolean isAdminMaster(String user) {
		try (ResultSet rs = executeQuery(SQLCommand.selectAll(DBConsts.TABLE_ADMINS, new String[] { "game", "room", "name" }, new Object[] { "", -1, user }))) {
			return rs.next();
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}

		return false;
	}

	public boolean isClosed() {
		return closer.isClosed();
	}

	public boolean isClosing() {
		return closer.isClosing();
	}

	public boolean isRunningInDestroyerQueue() {
		return destroyerQueue.isCurrentThread();
	}

	public boolean isSystem(String user) {
		try (ResultSet rs = executeQuery(SQLCommand.selectAll(DBConsts.TABLE_USERS, new String[] { "name" }, new Object[] { user }))) {
			if (rs.next())
				return rs.getBoolean("system");
		} catch (SQLException e) {
			handleException(e);
		} catch (InterruptedException e) {
			close();
		}

		return false;
	}

	public int login(String game, String username, String password, String ip, String[] sid) {
		try (CallableStatement stm = pool.getConnection().prepareCall("CALL Login(" + SQLFormater.formatValues(game, username, password, ip, new SQLExpression("?"), new SQLExpression("?")) + ")")) {
			stm.registerOutParameter(1, java.sql.Types.INTEGER);
			stm.registerOutParameter(2, java.sql.Types.VARCHAR);
			stm.execute();

			int result = stm.getInt(1);
			sid[0] = stm.getString(2);
			if (result == -1)
				throw new SQLException(sid[0]);

			return result;
		} catch (SQLException e) {
			sid[0] = e.getMessage();
			handleException(e);
		} catch (InterruptedException e) {
			sid[0] = e.getMessage();
		}

		return -1;
	}

	public void logout(String game, String username, String sid) {
		try {
			executeUpdate(SQLCommand.delete(DBConsts.TABLE_SESSIONS, new String[] { "game", "username", "sid" }, new Object[] { game, username, sid }));
		} catch (InterruptedException e) {
			close();
		} catch (SQLException e) {
			handleException(e);
		}
	}

	public void logToErr(String message) {
		log.logToErr(message);
	}

	public void logToErr(String prefix, String message) {
		log.logToErr(prefix, message);
	}

	public void logToErr(String prefix, String message, Throwable e) {
		log.logToErr(prefix, message, e);
	}

	public void logToErr(String message, Throwable e) {
		log.logToErr(message, e);
	}

	public void logToErr(Throwable e) {
		log.logToErr(e);
	}

	public void logToOut(String message) {
		logToOut(null, message);
	}

	public void logToOut(String prefix, String message) {
		log.logToOut(prefix, message);
	}

	public void logToOut(String prefix, String[] messages) {
		log.logToOut(prefix, messages);
	}

	public void logToOut(String[] messages) {
		log.logToOut(messages);
	}

	private boolean mute(int room, User user, long time, Set<BanType> type, String reason) {
		return addBan(room, user, time, type, BanRestriction.CHAT, reason);
	}

	private boolean mute(int room, User user, long time, Set<BanType> type, String admin, String reason) {
		return addBan(room, user, time, type, BanRestriction.CHAT, admin, reason);
	}

	public boolean mute(int room, User user, long time, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), reason);
	}

	public boolean mute(int room, User user, long time, String admin, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), admin, reason);
	}

	private boolean mute(User user, long time, Set<BanType> type, String reason) {
		return addBan(user, time, type, BanRestriction.CHAT, reason);
	}

	private boolean mute(User user, long time, Set<BanType> type, String admin, String reason) {
		return addBan(user, time, type, BanRestriction.CHAT, admin, reason);
	}

	public boolean mute(User user, long time, String reason) {
		return mute(user, time, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), reason);
	}

	public boolean mute(User user, long time, String admin, String reason) {
		return mute(user, time, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), admin, reason);
	}

	private boolean mute(User user, Set<BanType> type, String reason) {
		return addBan(user, type, BanRestriction.CHAT, reason);
	}

	public boolean mute(User user, String reason) {
		return mute(user, EnumSet.of(BanType.NICK, BanType.COMPID, BanType.IP), reason);
	}

	public boolean muteByCompid(int room, User user, long time, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.COMPID), reason);
	}

	public boolean muteByCompid(int room, User user, long time, String admin, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.COMPID), admin, reason);
	}

	public boolean muteByCompid(User user, long time, String reason) {
		return mute(user, time, EnumSet.of(BanType.COMPID), reason);
	}

	public boolean muteByCompid(User user, long time, String admin, String reason) {
		return mute(user, time, EnumSet.of(BanType.COMPID), admin, reason);
	}

	public boolean muteByCompid(User user, String reason) {
		return mute(user, EnumSet.of(BanType.COMPID), reason);
	}

	public boolean muteByCompidAndIP(int room, User user, long time, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.COMPID, BanType.IP), reason);
	}

	public boolean muteByCompidAndIP(int room, User user, long time, String admin, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.COMPID, BanType.IP), admin, reason);
	}

	public boolean muteByCompidAndIP(User user, long time, String reason) {
		return mute(user, time, EnumSet.of(BanType.COMPID, BanType.IP), reason);
	}

	public boolean muteByCompidAndIP(User user, long time, String admin, String reason) {
		return mute(user, time, EnumSet.of(BanType.COMPID, BanType.IP), admin, reason);
	}

	public boolean muteByCompidAndIP(User user, String reason) {
		return mute(user, EnumSet.of(BanType.COMPID, BanType.IP), reason);
	}

	public boolean muteByIP(int room, User user, long time, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.IP), reason);
	}

	public boolean muteByIP(int room, User user, long time, String admin, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.IP), admin, reason);
	}

	public boolean muteByIP(User user, long time, String reason) {
		return mute(user, time, EnumSet.of(BanType.IP), reason);
	}

	public boolean muteByIP(User user, long time, String admin, String reason) {
		return mute(user, time, EnumSet.of(BanType.IP), admin, reason);
	}

	public boolean muteByIP(User user, String reason) {
		return mute(user, EnumSet.of(BanType.IP), reason);
	}

	public boolean muteByName(int room, User user, long time, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.NICK), reason);
	}

	public boolean muteByName(int room, User user, long time, String admin, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.NICK), admin, reason);
	}

	public boolean muteByName(User user, long time, String reason) {
		return mute(user, time, EnumSet.of(BanType.NICK), reason);
	}

	public boolean muteByName(User user, long time, String admin, String reason) {
		return mute(user, time, EnumSet.of(BanType.NICK), admin, reason);
	}

	public boolean muteByName(User user, String reason) {
		return mute(user, EnumSet.of(BanType.NICK), reason);
	}

	public boolean muteByNameAndCompid(int room, User user, long time, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.NICK, BanType.COMPID), reason);
	}

	public boolean muteByNameAndCompid(int room, User user, long time, String admin, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.NICK, BanType.COMPID), admin, reason);
	}

	public boolean muteByNameAndCompid(User user, long time, String reason) {
		return mute(user, time, EnumSet.of(BanType.NICK, BanType.COMPID), reason);
	}

	public boolean muteByNameAndCompid(User user, long time, String admin, String reason) {
		return mute(user, time, EnumSet.of(BanType.NICK, BanType.COMPID), admin, reason);
	}

	public boolean muteByNameAndCompid(User user, String reason) {
		return mute(user, EnumSet.of(BanType.NICK, BanType.COMPID), reason);
	}

	public boolean muteByNameAndIP(int room, User user, long time, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.NICK, BanType.IP), reason);
	}

	public boolean muteByNameAndIP(int room, User user, long time, String admin, String reason) {
		return mute(room, user, time, EnumSet.of(BanType.NICK, BanType.IP), admin, reason);
	}

	public boolean muteByNameAndIP(User user, long time, String reason) {
		return mute(user, time, EnumSet.of(BanType.NICK, BanType.IP), reason);
	}

	public boolean muteByNameAndIP(User user, long time, String admin, String reason) {
		return mute(user, time, EnumSet.of(BanType.NICK, BanType.IP), admin, reason);
	}

	public boolean muteByNameAndIP(User user, String reason) {
		return mute(user, EnumSet.of(BanType.NICK, BanType.IP), reason);
	}

	public void notifyUpdated(Room lobby) {
		if (rooms.contains(lobby))
			synchronized (updatedRooms) {
				if (!updatedRooms.contains(lobby))
					updatedRooms.add(lobby);
			}
	}

	public void notifyUpdated(String name) {
		post(() -> {
			List<Room> rooms = rooms();
			for (Room room : rooms) {
				room.notifyUpdated(name);

				List<Table> tables = room.tables(name);
				for (Table table : tables) {
					table.notifyUpdated(name);
					table.notifyChanged();
				}
			}
		});
	}

	private void notifyUpdatedUsersToRooms() {
		List<Room> updateds;
		synchronized (updatedRooms) {
			if (updatedRooms.size() == 0)
				return;

			updateds = new ArrayList<>(updatedRooms);

			updatedRooms.clear();
		}

		synchronized (rooms) {
			for (Room room : rooms) {
				OutputProtocol protocol = room.prepareBroadCastProtocol();
				if (protocol != null)
					protocol.roomList(updateds, room, true);
			}
		}
	}

	public void open(String homeDir, Config config) throws IOException, SQLException, InterruptedException, ClassNotFoundException, DuplicateOpcodeException {
		InputProtocol inputProtocol = createInputProtocol(null);
		inputProtocol.check();
		OutputProtocol outputProtocol = createOutputProtocol(null);
		outputProtocol.check();
		
		if (closer != null && closer.isOpen())
			return;

		this.homeDir = homeDir;

		objectsClosed = 0;
		logDir = null;

		closer = new Closer(new Interruptable() {

			@Override
			public void close() {
				closeInternal();
			}

			@Override
			public void interrupt() {
				Server.this.interrupt();
			}

		});

		String gameName = getGameName();

		String dbDriverName = DBConsts.DEFAULT_DB_DRIVER_NAME;
		String dbDriverClass = DBConsts.DEFAULT_DB_DRIVER_CLASS;
		String dbHost = DBConsts.DEFAULT_DB_HOST;
		int dbPort = DBConsts.DEFAULT_DB_PORT;
		String dbName = DBConsts.DEFAULT_DB_NAME;
		String dbUserName = DBConsts.DEFAULT_DB_USERNAME;
		String dbPassword = DBConsts.DEFAULT_DB_PASSWORD;

		for (int i = 0; i < config.getChildCount(); i++) {
			Tree.Node<ConfigEntry> child = config.getChild(i);
			ConfigEntry entry = child.getValue();
			if (!(entry instanceof MapConfigEntry))
				continue;

			String name = entry.getName();
			Map<String, String> attrs = ((MapConfigEntry) entry).map();
			if (name.equalsIgnoreCase("log") && logDir == null) {
				logDir = attrs.getOrDefault("path", new File(homeDir, "logs").getAbsolutePath());
				createLogs(logDir);
			} else if (name.equals("db")) {
				dbDriverName = attrs.getOrDefault("driver_name", DBConsts.DEFAULT_DB_DRIVER_NAME);
				dbDriverClass = attrs.getOrDefault("driver_class", DBConsts.DEFAULT_DB_DRIVER_CLASS);
				dbHost = attrs.getOrDefault("host", DBConsts.DEFAULT_DB_HOST);
				String s = attrs.get("port");
				try {
					dbPort = s != null ? Integer.parseInt(s) : DBConsts.DEFAULT_DB_PORT;
				} catch (NumberFormatException e) {
					dbPort = DBConsts.DEFAULT_DB_PORT;
				}
				dbName = attrs.getOrDefault("name", DBConsts.DEFAULT_DB_NAME);
				dbUserName = attrs.getOrDefault("username", DBConsts.DEFAULT_DB_USERNAME);
				dbPassword = attrs.getOrDefault("password", DBConsts.DEFAULT_DB_PASSWORD);
			}
		}

		if (logDir == null)
			createLogs(new File(homeDir, "logs").getAbsolutePath());

		connections = new Vector<>();
		names = new Hashtable<>();
		rooms = new Vector<>();
		updatedRooms = new Vector<>();

		pool = new SQLConnectionPool(dbDriverName, dbDriverClass, dbHost, dbPort, dbName, dbUserName, dbPassword);

		try {
			IOException error = executeTransaction((connection) -> {
				try (ResultSet rs = connection.selectAll(DBConsts.TABLE_GAMES, new String[] { "name" }, new Object[] { gameName })) {
					if (!rs.next())
						throw new RuntimeException("Game '" + gameName + "' not found in table '" + DBConsts.TABLE_GAMES + "'.");

					host = rs.getString("host");
					port = rs.getInt("port");
					title = rs.getString("title");
				}

				try {
					InetAddress addr = InetAddress.getByName(host);
					server = new ServerSocket(port, 50, addr);
				} catch (IOException e) {
					return e;
				}

				try (ResultSet rs = connection.executeQuery("SELECT * FROM " + DBConsts.TABLE_USERS + " WHERE ingame_chips > 0", ResultSet.FETCH_FORWARD, ResultSet.CONCUR_UPDATABLE)) {
					while (rs.next()) {
						long available = rs.getLong("chips");
						rs.updateLong("chips", available + rs.getLong("ingame_chips"));
						rs.updateLong("ingame_chips", 0);
						rs.updateRow();
					}
				}

				return null;
			});
			if (error != null)
				throw error;
		} catch (Throwable e) {
			pool.destroy();
			pool = null;

			throw e;
		}

		group = new ThreadGroup(title);

		objectsToClose = 2;
		destroyerQueue = new ProcessQueue(group, title + " destroyer queue");
		destroyerQueue.addCloseListener(() -> checkClosed());
		queue = new ProcessQueue(group, title + " server queue");

		Throwable exception = queue.postAndWait(() -> {
			try {
				executeTransaction((connection) -> {
					try (ResultSet rs = connection.selectAll(DBConsts.TABLE_ROOMS, new String[] { "game" }, new Object[] { getGameName() }, "number")) {
						int index = 1;
						while (rs.next()) {
							int number = rs.getInt("number");
							String name = rs.getString("name");
							int maxPlayers = rs.getShort("max_players");
							boolean withAdmin = rs.getBoolean("with_admin");
							boolean isPublic = rs.getBoolean("public");
							boolean active = rs.getBoolean("active");

							if (!active)
								continue;

							Room room;
							try {
								room = getRoomClass().newInstance();
							} catch (Throwable e) {
								logToErr("Could not create the room instance", e);

								continue;
							}

							try {
								room.open(this, index++, number, name, maxPlayers, withAdmin, isPublic);
							} catch (IOException e) {
								return e;
							}

							rooms.add(room);
						}
					}

					return null;
				});
			} catch (SQLException | InterruptedException e) {
				return e;
			}

			return null;
		});

		if (exception != null)
			if (exception instanceof SQLException)
				throw (SQLException) exception;
			else if (exception instanceof InterruptedException)
				throw (InterruptedException) exception;

		tmrNotifyRoomList = new Timer(queue, 10000, (e) -> logToErr(e));
		tmrNotifyRoomList.addListener((timer, interval) -> notifyUpdatedUsersToRooms());

		tmbUpdateDB = new Timer(queue, 20000, (e) -> logToErr(e));
		tmbUpdateDB.addListener((sender, interval) -> updateUserCountInDB());

		acceptorQueue = new ProcessQueue(group, "Server acceptor queue");
		acceptorQueue.post(() -> accept(), 0, (e) -> logToErr(e));
	}

	public void post(NonReturnableProcessWithoutArg process) {
		queue.post(process, (e) -> logToErr(e));
	}

	public void postAndWait(NonReturnableProcessWithoutArg process) throws InterruptedException {
		try {
			queue.postAndWait(process);
		} catch (RuntimeException e) {
			logToErr(e);
		}
	}

	public void postRoomList(User user) {
		OutputProtocol protocol = user.prepareProtocol();
		if (protocol != null)
			protocol.roomList(rooms(), user.getBase(), false);
	}

	public int register(String username, String password, String email, String[] errorMessage) {
		try (CallableStatement stm = pool.getConnection().prepareCall("CALL Register(" + SQLFormater.formatValues(username, password, email, new SQLExpression("?"), new SQLExpression("?")) + ")")) {
			stm.registerOutParameter(1, java.sql.Types.INTEGER);
			stm.registerOutParameter(2, java.sql.Types.VARCHAR);
			stm.execute();

			int result = stm.getInt(1);
			if (result == -1)
				throw new SQLException(stm.getString(2));

			return result;
		} catch (SQLException e) {
			errorMessage[0] = e.getMessage();
			handleException(e);
		} catch (InterruptedException e) {
			errorMessage[0] = e.getMessage();
		}

		return -1;
	}

	public void removeConnection(Connection connection, boolean normalExit) {
		if (!connections.remove(connection))
			return;

		post(() -> {
			if (normalExit)
				connection.close();
			else
				connection.closeAndSetReconnecting();
		});
	}

	public List<Room> rooms() {
		synchronized (rooms) {
			return new ArrayList<>(rooms);
		}
	}

	public void send(NonReturnableProcessWithoutArg process) {
		try {
			queue.send(process);
		} catch (InterruptedException e) {
			close();
		} catch (RuntimeException e) {
			logToErr(e);
		}
	}

	public <T> T send(ReturnableProcess<T> process) {
		try {
			return queue.send(process);
		} catch (InterruptedException e) {
			close();
		} catch (RuntimeException e) {
			logToErr(e);
		}

		return null;
	}

	public abstract String tableStats();

	private void updateUserCountInDB() {
		try {
			executeUpdate(SQLCommand.update(DBConsts.TABLE_GAMES, new String[] { "name" }, new Object[] { getGameName() }, new String[] { "users" }, new Object[] { getUserCount() }));
		} catch (InterruptedException e) {
			close();
		} catch (SQLException e) {
			handleException(e);
		}
	}

}
