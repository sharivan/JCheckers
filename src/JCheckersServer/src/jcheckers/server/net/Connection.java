package jcheckers.server.net;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import common.db.SQLCommand;
import common.process.Closer;
import common.process.Interruptable;
import common.process.NonReturnableProcessWithoutArg;
import common.process.ProcessQueue;
import common.process.ReturnableProcess;
import common.process.TimeOutException;
import common.process.timer.Timer;
import common.util.DigestUtil;
import common.util.DumpUtil;
import common.util.ParserUtil;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.io.JCheckersEOFException;
import jcheckers.common.io.JCheckersIOException;
import jcheckers.common.io.JCheckersInputStream;
import jcheckers.common.io.JCheckersOutputStream;
import jcheckers.common.util.CryptUtil;
import jcheckers.server.io.InputProtocol;
import jcheckers.server.io.OutputProtocol;

public final class Connection implements Interruptable {

	private class AcceptedConnectionOutputStream extends JCheckersDataOutputStream {

		private ByteArrayOutputStream out;

		private AcceptedConnectionOutputStream(ByteArrayOutputStream out) {
			super(out);

			this.out = out;
		}

		@Override
		public void close() {
			super.close();

			out = null;
		}

		@Override
		protected void flushInternal(boolean close) {
			Connection.this.postBlock(out.toByteArray(), close);
		}

	}

	private static final boolean DEBUG_INPUT = false;
	private static final boolean DEBUG_OUTPUT = false;

	static final int TEST_CONNECTION_INTERVAL = 15000; // 15 segundos
	static final int CHECK_CONNECTION_INTERVAL = 60000; // 1 minuto

	public static final int CREATED = 0;
	public static final int OPENING = 1;
	public static final int AUTHENTICATING = 2;
	public static final int OPEN = 3;
	public static final int CLOSING = 4;

	private static final int RECEIVE_CHAT_MAX_MESSAGES = 6;
	private static final int RECEIVE_CHAT_INTERVAL = 2 * 1000; // 2 segundos
	private static final int DEFAULT_MAX_MESSAGES = 50;
	private static final int DEFAULT_INTERVAL = 5 * 1000; // 5 segundos

	private static final int CHAT_FLOOD_MUTE_TIME = 60 * 1000; // 1 minuto
	private static final int CHAT_FLOOD_BAN_TIME = 10 * 60 * 1000; // 10 minutos
	private static final int GENERIC_FLOOD_BAN_TIME = 10 * 60 * 1000; // 10
																		// minutos

	private static final int SPAM_BLOCK_INTERVAL = 10 * 1000;

	private Server server;
	private Socket socket;

	private User user;
	private int state;
	private int objectsClosed;
	private boolean reconnecting;
	private Closer closer;
	private JCheckersInputStream input;
	private JCheckersOutputStream output;
	private ThreadGroup group;
	private ProcessQueue inputQueue;
	private ProcessQueue outputQueue;
	private Timer tmrTestConnection;
	private Timer tmrCheckConnection;
	private long lastReceivedTime;
	private List<SpamCheck>[] spamCheck;
	private boolean isMutedByFlood;

	@SuppressWarnings("unchecked")
	protected Connection(Server server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;

		lastReceivedTime = -1;
		isMutedByFlood = false;

		spamCheck = new List[256];
		for (int i = 0; i < spamCheck.length; i++) {
			List<SpamCheck> item = new ArrayList<>();
			spamCheck[i] = item;
			if (i == InputProtocol.RECEIVE_CHAT)
				item.add(new SpamCheck(RECEIVE_CHAT_MAX_MESSAGES, RECEIVE_CHAT_INTERVAL, (sc) -> {
					if (isMutedByFlood)
						server.ban(user, CHAT_FLOOD_BAN_TIME, "chat flood");
					else {
						isMutedByFlood = true;
						server.mute(user, CHAT_FLOOD_MUTE_TIME, "chat flood");
					}
					sc.block(SPAM_BLOCK_INTERVAL);
				}));
			if (i != InputProtocol.NO_CRYPT_NEXT_BLOCK) {
				String reason = "generic command flood: " + i;
				item.add(new SpamCheck(DEFAULT_MAX_MESSAGES, DEFAULT_INTERVAL, (sc) -> {
					server.ban(user, GENERIC_FLOOD_BAN_TIME, reason);
					sc.block(SPAM_BLOCK_INTERVAL);
				}));
			}
		}

		group = new ThreadGroup(server.getGroup(), "Connection " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

		inputQueue = new ProcessQueue(group, "Connection input queue");
		outputQueue = new ProcessQueue(group, "Connection output queue");
		outputQueue.addCloseListener(() -> onClose());

		tmrTestConnection = new Timer(outputQueue, TEST_CONNECTION_INTERVAL, true, (e) -> server.logToErr(toString(), null, e));
		tmrTestConnection.addListener((timer, interval) -> Connection.this.server.createOutputProtocol(prepareOutput()).pingConnection());

		tmrCheckConnection = new Timer(outputQueue, CHECK_CONNECTION_INTERVAL, true, (e) -> server.logToErr(toString(), null, e));
		tmrCheckConnection.addListener((timer, interval) -> closeAndSetReconnecting());

		state = OPENING;
		reconnecting = false;

		closer = new Closer(new Interruptable() {

			@Override
			public void close() {
				closeInternal();
			}

			@Override
			public void interrupt() {
				Connection.this.interrupt();
			}

		});

		inputQueue.post(() -> {
			try {
				input = new JCheckersInputStream(Connection.this.socket.getInputStream(), Connection.this.server.getGameID());
				output = new JCheckersOutputStream(new BufferedOutputStream(Connection.this.socket.getOutputStream()), Connection.this.server.getGameID());

				if (server.isClosing()) {
					OutputProtocol protocol = new OutputProtocol(prepareOutput());
					protocol.serverShuttingDown();
					postClose();

					return;
				}

				synchronized (this) {
					state = AUTHENTICATING;
				}

				input.noDecryptNextBlock();
				byte[] buf = input.readEntireBlock();

				if (DEBUG_INPUT)
					synchronized (System.out) {
						System.out.println("// Received from " + toString());
						System.out.println(DumpUtil.writeDump(buf, 0, buf.length));
						System.out.println();
					}

				JCheckersDataInputStream block = new JCheckersDataInputStream(new ByteArrayInputStream(buf));
				InputProtocol protocol = server.createInputProtocol(block);
				int opcode = protocol.getOpcode(block.readUChar());

				if (opcode != InputProtocol.HELLO)
					throw new IOException("Invalid \"hello\" opcode " + opcode);

				String localIP = block.readEasyString();
				String pcName = block.readEasyString();
				String username = block.readEasyString();
				String room = block.readEasyString();
				int compid = block.readInt();

				Room lobby;
				Table table;
				int roomID = ParserUtil.parseInt(room, -1);

				lobby = roomID != -1 ? Connection.this.server.getLobbyByID(roomID) : null;

				if (lobby == null) {
					Connection.this.server.createOutputProtocol(prepareOutput()).invalidRoom();
					postClose();

					return;
				}

				byte[] key = CryptUtil.computeKey(pcName);
				input.setKey(key);
				output.setKey(key);

				buf = input.readEntireBlock();

				if (DEBUG_INPUT)
					synchronized (System.out) {
						System.out.println("// Received from " + toString());
						System.out.println(DumpUtil.writeDump(buf, 0, buf.length));
						System.out.println();
					}

				block = new JCheckersDataInputStream(new ByteArrayInputStream(buf));
				protocol = server.createInputProtocol(block);
				opcode = protocol.getOpcode(block.readUChar());

				String sid = block.readString();

				long banExpires = server.getBanExpiresByNameOrCompidOrIP(roomID, username, compid, getIP().getHostAddress());
				if (banExpires != 0) {
					Calendar calendar = Calendar.getInstance();
					if (banExpires != -1)
						calendar.setTimeInMillis(banExpires);
					Connection.this.server.createOutputProtocol(prepareOutput())
							.notifyBannedAndDisconnect(banExpires != -1 ? new SimpleDateFormat("dd/MM/yyyy HH:mm z").format(calendar.getTime()) : "");
					postClose();

					return;
				}

				boolean found = false;
				try (ResultSet rs = getServer()
						.executeQuery(SQLCommand.selectAll(DBConsts.TABLE_SESSIONS, new String[] { "game", "login", "session_id" }, new Object[] { getServer().getGameName(), username, sid }))) {
					while (rs.next()) {
						Timestamp created = rs.getTimestamp("created");
						int expires = rs.getInt("expires");

						if (expires == -1 || created.getTime() + expires * 1000 > System.currentTimeMillis()) {
							found = true;

							break;
						}
					}
				} catch (SQLException e) {
					handleException(e);

					return;
				}

				if (!found)
					try (ResultSet rs = getServer().executeQuery(SQLCommand.selectAll(DBConsts.TABLE_USERS, new String[] { "name" }, new Object[] { username }))) {
						if (!rs.next()) {
							Connection.this.server.createOutputProtocol(prepareOutput()).invalidPassword();
							postClose();

							return;
						}

						String password = rs.getString("password");
						if (!sid.equalsIgnoreCase(DigestUtil.md5(password))) {
							Connection.this.server.createOutputProtocol(prepareOutput()).invalidPassword();
							postClose();

							return;
						}
					} catch (SQLException e) {
						handleException(e);

						return;
					}

				getServer().executeTransaction((connection) -> {
					try (ResultSet rs = connection.insert(DBConsts.TABLE_ACCESS_LOG, true,
							new Object[] { null, username, new Timestamp(System.currentTimeMillis()), compid, getIP().getHostAddress(), Connection.this.server.getGameName(), roomID })) {
						if (!rs.next())
							throw new SQLException("Insert into table '" + DBConsts.TABLE_ACCESS_LOG + "' not generated a new key.");

						int lastInsertID = rs.getInt(1);

						connection.update(DBConsts.TABLE_USERS, new String[] { "name" }, new Object[] { username }, new String[] { "last_access" }, new Object[] { lastInsertID });
					}

					return null;
				});

				switch (opcode) {
					case InputProtocol.CREATE_TABLE:
						user = Connection.this.server.getUserClass().newInstance();
						user.setup(server, Connection.this, username, pcName, localIP, compid);
						user.fetchData();

						table = lobby.createTable(user, block);

						if (table == null) {
							Connection.this.server.createOutputProtocol(prepareOutput()).tableNotExist();
							postClose();

							return;
						}

						break;
					case InputProtocol.JOIN_TABLE_SITING:
					case InputProtocol.JOIN_TABLE_WATCHING:
					case InputProtocol.JOIN_TABLE_RECONNECTED_SITING:
					case InputProtocol.JOIN_TABLE_RECONNECTED_WATCHING:

						int tableId = block.readInt();
						// int sitIndex = opcode ==
						// InputProtocol.JOIN_TABLE_SITING || opcode ==
						// InputProtocol.JOIN_TABLE_RECONNECTED_SITING ?
						// block.readUChar() : -1;
						int sitIndex = block.readUChar();
						block.readInt();

						table = lobby.getTableByID(tableId);
						if (table == null) {
							Connection.this.server.createOutputProtocol(prepareOutput()).tableNotExist();
							postClose();

							return;
						}

						user = table.getReconnecting(username);
						if (user == null) {
							user = Connection.this.server.getUserClass().newInstance();
							user.setup(server, Connection.this, username, pcName, localIP, compid);
						} else
							user.setConnection(Connection.this);
						user.fetchData();

						if (!table.join(user, sitIndex, opcode)) {
							close();

							return;
						}

						break;
					case InputProtocol.JOIN_ROOM:
						user = lobby.getReconnecting(username);
						if (user == null) {
							user = Connection.this.server.getUserClass().newInstance();
							user.setup(server, Connection.this, username, pcName, localIP, compid);
						} else
							user.setConnection(Connection.this);
						user.fetchData();

						table = null;
						if (!lobby.join(user)) {
							close();

							return;
						}

						Connection.this.server.postRoomList(user);

						break;
					default:
						input.throwIOException("Invalid \"access\" opcode " + opcode);
				}

				tmrTestConnection.play();
				tmrCheckConnection.play();
				lastReceivedTime = System.currentTimeMillis();

				List<Connection> connections = server.names.get(username);
				if (connections == null) {
					connections = new ArrayList<>();
					connections.add(Connection.this);
					server.names.put(username, connections);
				} else
					connections.add(Connection.this);

				inputQueue.setName("Connection input queue [" + username + "]");
				outputQueue.setName("Connection output queue [" + username + "]");

				synchronized (this) {
					state = OPEN;
				}
			} catch (Throwable e) {
				handleException(e);
			}
		}, (e) -> handleException(e));

		inputQueue.post(() -> {
			if (getState() != OPEN) {
				close();

				return;
			}

			JCheckersDataInputStream block = null;
			try {
				byte[] buf = input.readEntireBlock();
				block = new JCheckersDataInputStream(new ByteArrayInputStream(buf));

				tmrCheckConnection.reset();

				InputProtocol protocol = server.createInputProtocol(block);
				int opcode = protocol.getOpcode(block.readUChar());

				List<SpamCheck> list = spamCheck[opcode];
				for (SpamCheck sc : list) {
					if (sc.isBlocked())
						return;
					if (!sc.check())
						return;
				}

				if (DEBUG_INPUT)
					if (opcode != InputProtocol.PING_CONNECTION)
						synchronized (System.out) {
							System.out.println("// Received from " + toString());
							System.out.println(DumpUtil.writeDump(buf, 0, buf.length));
							System.out.println();
						}

				User user = Connection.this.user;
				if (user == null) {
					close();

					return;
				}

				user.parseData(opcode, block);

				if (opcode != InputProtocol.NO_CRYPT_NEXT_BLOCK && opcode != InputProtocol.CHANGE_KEY && opcode != InputProtocol.PING_CONNECTION && opcode != InputProtocol.PONG
						&& opcode != InputProtocol.INVITATION_AUTO_REJECTED)
					lastReceivedTime = System.currentTimeMillis();
			} catch (Throwable e) {
				handleException(e);
			} finally {
				if (block != null)
					try {
						block.close();
					} catch (IOException e) {
					}
			}
		}, 0, (e) -> handleException(e));
	}

	@Override
	public void close() {
		closeNormal();
	}

	public void close(boolean wait) throws TimeOutException {
		closeNormal(wait);
	}

	public void close(boolean wait, boolean force) throws TimeOutException {
		closeNormal(wait, force);
	}

	public void close(boolean wait, int timeout) throws TimeOutException {
		closeNormal(wait, timeout);
	}

	public void close(boolean wait, int timeout, boolean force) throws TimeOutException {
		closeNormal(wait, timeout, force);
	}

	public void closeAndSetReconnecting() {
		if (!startClosing(false))
			return;

		server.close(closer);
	}

	public void closeAndSetReconnecting(boolean wait) throws TimeOutException {
		if (!startClosing(false))
			return;

		server.close(closer, wait);
	}

	public void closeAndSetReconnecting(boolean wait, boolean force) throws TimeOutException {
		if (!startClosing(false))
			return;

		server.close(closer, wait, force);
	}

	public void closeAndSetReconnecting(boolean wait, int timeout) throws TimeOutException {
		if (!startClosing(false))
			return;

		server.close(closer, wait, timeout);
	}

	public void closeAndSetReconnecting(boolean wait, int timeout, boolean force) throws TimeOutException {
		if (!startClosing(false))
			return;

		server.close(closer, wait, timeout, force);
	}

	protected void closeInternal() {
		Server server = this.server;
		if (server.isClosing() && user != null) {
			OutputProtocol protocol = user.prepareProtocol();
			if (protocol != null)
				protocol.serverShuttingDown();
		}

		server.removeConnection(this, !reconnecting);

		if (user != null) {
			if (!reconnecting) {
				String name = user.getName();
				List<Connection> connections = server.names.get(name);
				if (connections != null) {
					connections.remove(this);
					if (connections.size() == 0)
						server.names.remove(name);
				}
			}

			user.closeInternal(!reconnecting);

			user = null;
		}

		closeObjects();

		objectsClosed = 0;

		server.logToOut(toString(), "Closing input queue...");
		try {
			inputQueue.close(true, true);
		} catch (TimeOutException e) {
			server.logToErr(toString(), "WARNING: The input queue was forced closed.");
		} finally {
			server.logToOut(toString(), "Closing input closed.");
			objectsClosed++;
		}

		server.logToOut(toString(), "Closing output queue...");
		try {
			outputQueue.close(true, true);
		} catch (TimeOutException e) {
			server.logToErr(toString(), "WARNING: The output queue was forced closed.");
		} finally {
			server.logToOut(toString(), "Closing output closed.");
			objectsClosed++;
		}
	}

	public void closeNormal() {
		if (!startClosing(true))
			return;

		server.close(closer);
	}

	public void closeNormal(boolean wait) throws TimeOutException {
		if (!startClosing(true))
			return;

		server.close(closer, wait);
	}

	public void closeNormal(boolean wait, boolean force) throws TimeOutException {
		if (!startClosing(true))
			return;

		server.close(closer, wait, force);
	}

	public void closeNormal(boolean wait, int timeout) throws TimeOutException {
		if (!startClosing(true))
			return;

		server.close(closer, wait, timeout);
	}

	public void closeNormal(boolean wait, int timeout, boolean force) throws TimeOutException {
		if (!startClosing(true))
			return;

		server.close(closer, wait, timeout, force);
	}

	private void closeObjects() {
		tmrCheckConnection.close();
		tmrTestConnection.close();

		if (input != null)
			try {
				input.close();
			} catch (IOException e) {
			}

		if (output != null)
			try {
				output.close();
			} catch (IOException e) {
			}

		if (socket != null)
			try {
				socket.close();
			} catch (IOException e) {
			}
	}

	public int closingMaxScore() {
		return 2;
	}

	public int closingScore() {
		return objectsClosed;
	}

	public void flush() {
		post(() -> {
			try {
				output.flush();
			} catch (IOException e) {
				handleIOException(e);
			}
		});
	}

	public long getInactiveTime() {
		return System.currentTimeMillis() - lastReceivedTime;
	}

	protected ProcessQueue getInputQueue() {
		return inputQueue;
	}

	public InetAddress getIP() {
		return socket.getInetAddress();
	}

	protected ProcessQueue getOutputQueue() {
		return getOutputQueue();
	}

	public int getPort() {
		return socket.getPort();
	}

	public Server getServer() {
		return server;
	}

	public synchronized int getState() {
		return state;
	}

	public User getUser() {
		return user;
	}

	protected void handleException(Throwable e) {
		if (e instanceof JCheckersIOException)
			handleJCheckersIOException((JCheckersIOException) e);
		else if (e instanceof IOException)
			handleIOException((IOException) e);
		else if (e instanceof SQLException)
			handleSQLException((SQLException) e);
		else {
			server.logToErr(toString(), null, e);
			closeAndSetReconnecting();
		}
	}

	protected void handleIOException(IOException e) {
		closeAndSetReconnecting();

		if (e instanceof EOFException)
			return;

		if (e instanceof SocketTimeoutException)
			return;

		if (e instanceof SocketException)
			return;

		server.logToErr(toString(), null, e);
	}

	protected void handleJCheckersIOException(JCheckersIOException e) {
		closeAndSetReconnecting();

		if (e instanceof JCheckersEOFException)
			return;

		Throwable cause = e.getCause();
		if (cause != null) {
			if (cause instanceof EOFException)
				return;

			if (cause instanceof SocketTimeoutException)
				return;

			if (cause instanceof SocketException)
				return;
		}

		byte[] buf = e.getBuf();
		server.logToErr(toString(), buf != null ? DumpUtil.writeDump(buf) : null, e);
	}

	protected void handleSQLException(SQLException e) {
		server.logToErr(toString(), null, e);
	}

	@Override
	public void interrupt() {
		closeObjects();

		group.interrupt();

		server.logToErr(toString(), "WARNING: All threads were interrupted.");
	}

	public synchronized boolean isClosed() {
		return state == CLOSING && closer.isClosed();
	}

	public synchronized boolean isClosing() {
		return state == CLOSING && closer.isClosing();
	}

	public void noDecryptNextBlock() {
		input.noDecryptNextBlock();
	}

	public void noEncryptNextBlock() {
		output.noEncryptNextBlock();
	}

	private void onClose() {
		closeObjects();

		closer.stopClosing();

		server = null;
	}

	public void post(NonReturnableProcessWithoutArg process) {
		outputQueue.post(process, (e) -> server.logToErr(Connection.this.toString(), null, e));
	}

	public void postAndWait(NonReturnableProcessWithoutArg process) throws InterruptedException {
		try {
			outputQueue.postAndWait(process);
		} catch (RuntimeException e) {
			server.logToErr(toString(), null, e);
		}
	}

	public void postBlock(byte[] buf) {
		postBlock(buf, 0, buf.length, true);
	}

	public void postBlock(byte[] buf, boolean flush) {
		postBlock(buf, 0, buf.length, flush);
	}

	public void postBlock(byte[] buf, int off, int len) {
		postBlock(buf, off, len, true);
	}

	public void postBlock(byte[] buf, int off, int len, boolean flush) {
		post(() -> {
			if (DEBUG_OUTPUT) {
				OutputProtocol protocol = server.createOutputProtocol(null);
				if (buf[0] != protocol.getOpcode(OutputProtocol.PING_CONNECTION))
					synchronized (System.out) {
						System.out.println("// Sent to " + toString());
						System.out.println(DumpUtil.writeDump(buf, off, len));
						System.out.println();
					}
			}

			output.write(buf, off, len);
			if (flush) {
				try {
					output.flush();
				} catch (IOException e) {
					handleIOException(e);

					return;
				}
				tmrTestConnection.reset();
			} else
				output.flushBlock();
		});

	}

	public void postClose() {
		post(() -> closeNormal());
	}

	public void postCloseAndSetReconnecting() {
		post(() -> closeAndSetReconnecting());
	}

	public JCheckersDataOutputStream prepareOutput() {
		return new AcceptedConnectionOutputStream(new ByteArrayOutputStream());
	}

	public void send(NonReturnableProcessWithoutArg process) {
		try {
			outputQueue.send(process);
		} catch (InterruptedException e) {
			close();
		} catch (RuntimeException e) {
			server.logToErr(toString(), null, e);
		}
	}

	public <T> T send(ReturnableProcess<T> process) {
		try {
			return outputQueue.send(process);
		} catch (InterruptedException e) {
			close();
		} catch (RuntimeException e) {
			server.logToErr(toString(), null, e);
		}

		return null;
	}

	public void setInputMethod(int method) {
		input.setMethod(method);
	}

	public void setOutputMethod(int method) {
		output.setMethod(method);
	}

	private synchronized boolean startClosing(boolean normalExit) {
		if (state == CLOSING)
			return false;

		state = CLOSING;
		reconnecting = !normalExit;

		return true;
	}

	@Override
	public String toString() {
		return "connection" + (user != null ? " " + user.getName() : "") + " [" + getIP().getHostAddress() + ":" + getPort() + "]";
	}

}
