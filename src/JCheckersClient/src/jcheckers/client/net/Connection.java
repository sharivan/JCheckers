package jcheckers.client.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import common.io.Log;
import common.process.NonReturnableProcessWithoutArg;
import common.process.ProcessQueue;
import common.process.timer.Timer;
import common.util.DebugUtil;
import common.util.DumpUtil;
import jcheckers.client.io.InputProtocol;
import jcheckers.client.io.OutputProtocol;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.io.JCheckersEOFException;
import jcheckers.common.io.JCheckersIOException;
import jcheckers.common.io.JCheckersInputStream;
import jcheckers.common.io.JCheckersOutputStream;
import jcheckers.common.util.CryptUtil;

public abstract class Connection {

	public enum ConnectionMode {
		CREATE_TABLE, JOIN_TABLE_SITTING, JOIN_TABLE_WATCHING, JOIN_TABLE_RECONNECTING_SITTING, JOIN_TABLE_RECONNECTING_WATCHING, JOIN_LOBBY
	}

	private class ConnectionOutputStream extends JCheckersDataOutputStream {

		private ByteArrayOutputStream out;

		private ConnectionOutputStream(ByteArrayOutputStream out) {
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

	public enum ConnectionStatus {
		OPENNING, OPEN, CLOSING, CLOSED
	}

	static final int TEST_CONNECTION_INTERVAL = 15000; // 15 segundos
	static final int CHECK_CONNECTION_INTERVAL = 60000; // 1 minuto

	private static final boolean DEBUG_INPUT = true;
	private static final boolean DEBUG_OUTPUT = true;

	private String host;
	private int port;
	private String username;
	private String sid;
	private int roomID;
	private int tableID = -1;
	private int sitIndex = -1;
	private TableParams params;

	private String pcName = "";
	private int compid = -1;

	private Socket socket;
	private JCheckersInputStream in;
	private JCheckersOutputStream out;
	private ProcessQueue inputQueue;
	private ProcessQueue outputQueue;
	private Timer tmrTestConnection;
	private Timer tmrCheckConnection;
	private long lastReceivedTime = -1;

	ArrayList<ConnectionListener> listeners;

	private ConnectionStatus status = ConnectionStatus.OPENNING;
	private ConnectionMode mode = ConnectionMode.JOIN_LOBBY;
	private Throwable error;
	private boolean reconnecting;

	private int myID = -1;
	private User myUser;
	private Base base;
	private Room room;
	private OpenTable table;

	public Connection(String host, int port, String username, String sid) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.sid = sid;

		listeners = new ArrayList<>();

		inputQueue = new ProcessQueue("Input Queue");
		outputQueue = new ProcessQueue("Output Queue");

		inputQueue.addExceptionListener((e) -> onError(e));
		outputQueue.addExceptionListener((e) -> onError(e));

		tmrTestConnection = new Timer(outputQueue, TEST_CONNECTION_INTERVAL, true, (e) -> logToErr(toString(), null, e));
		tmrTestConnection.addListener((timer, interval) -> Connection.this.createOutputProtocol(prepareOutput()).pingConnection());

		tmrCheckConnection = new Timer(outputQueue, CHECK_CONNECTION_INTERVAL, true, (e) -> logToErr(toString(), null, e));
		tmrCheckConnection.addListener((timer, interval) -> close(false));
	}

	public void addListener(ConnectionListener listener) {
		listeners.add(listener);
	}

	public void close() {
		close(true);
	}

	public void close(boolean normalExit) {
		synchronized (this) {
			if (status == ConnectionStatus.CLOSING || status == ConnectionStatus.CLOSED)
				return;

			status = ConnectionStatus.CLOSING;
			reconnecting = !normalExit;
		}

		inputQueue.close();
		outputQueue.close();

		if (in != null)
			try {
				in.close();
			} catch (IOException e) {
			}

		if (out != null)
			try {
				out.close();
			} catch (IOException e) {
			}

		if (socket != null)
			try {
				socket.close();
			} catch (IOException e) {
			}
	}

	protected abstract InputProtocol createInputProtocol(JCheckersDataInputStream input);

	protected abstract OutputProtocol createOutputProtocol(JCheckersDataOutputStream output);

	protected abstract Room createRoom();

	protected abstract OpenTable createTable();

	protected abstract Table createTable(int roomID, int id, JCheckersDataInputStream in) throws JCheckersIOException;

	public void createTable(int room, TableParams params) {
		synchronized (this) {
			if (status != ConnectionStatus.OPENNING)
				return;
		}

		mode = ConnectionMode.CREATE_TABLE;
		roomID = room;
		this.params = params;

		inputQueue.post(() -> onOpen(), (e) -> handleException(e));
	}

	protected abstract User createUser();

	public Base getBase() {
		return base;
	}

	public synchronized Throwable getError() {
		return error;
	}

	public abstract int getGameID();

	public String getHost() {
		return host;
	}

	public long getInactiveTime() {
		return System.currentTimeMillis() - lastReceivedTime;
	}

	public ConnectionMode getMode() {
		return mode;
	}

	public int getMyID() {
		return myID;
	}

	public User getMyUser() {
		return myUser;
	}

	public TableParams getParams() {
		return params;
	}

	public int getPort() {
		return port;
	}

	public Room getRoom() {
		return room;
	}

	public int getRoomID() {
		return roomID;
	}

	public String getSID() {
		return sid;
	}

	public synchronized ConnectionStatus getStatus() {
		return status;
	}

	public OpenTable getTable() {
		return table;
	}

	public int getTableID() {
		return tableID;
	}

	public String getUsername() {
		return username;
	}

	protected void handleException(Throwable e) {
		if (e instanceof JCheckersIOException)
			handleJCheckersIOException((JCheckersIOException) e);
		else if (e instanceof IOException)
			handleIOException((IOException) e);
		else {
			logToErr(toString(), null, e);

			for (ConnectionListener listener : listeners)
				if (listener != null)
					try {
						listener.onError(this, e);
					} catch (Throwable e1) {
						e1.printStackTrace();
					}

			close(false);
		}
	}

	protected void handleIOException(IOException e) {
		close(false);

		if (e instanceof EOFException)
			return;

		if (e instanceof SocketTimeoutException)
			return;

		if (e instanceof SocketException)
			return;

		logToErr(toString(), null, e);

		for (ConnectionListener listener : listeners)
			if (listener != null)
				try {
					listener.onError(this, e);
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
	}

	protected void handleJCheckersIOException(JCheckersIOException e) {
		close(false);

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
		logToErr(toString(), buf != null ? DumpUtil.writeDump(buf) : null, e);

		for (ConnectionListener listener : listeners)
			if (listener != null)
				try {
					listener.onError(this, e);
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
	}

	public boolean isReconnecting() {
		return reconnecting;
	}

	public void joinLobby(int room) {
		synchronized (this) {
			if (status != ConnectionStatus.OPENNING)
				return;
		}

		mode = ConnectionMode.JOIN_LOBBY;
		roomID = room;

		inputQueue.post(() -> onOpen());
	}

	public void joinTable(int room, int table) {
		joinTable(room, table, -1);
	}

	public void joinTable(int room, int table, int seat) {
		synchronized (this) {
			if (status != ConnectionStatus.OPENNING)
				return;
		}

		mode = seat != -1 ? ConnectionMode.JOIN_TABLE_SITTING : ConnectionMode.JOIN_TABLE_WATCHING;
		roomID = room;
		tableID = table;
		sitIndex = seat;

		inputQueue.post(() -> onOpen(), (e) -> handleException(e));
	}

	public void logToErr(String message) {
		logToErr(null, message, null);
	}

	public void logToErr(String prefix, String message) {
		logToErr(prefix, message, null);
	}

	public synchronized void logToErr(String prefix, String message, Throwable e) {
		Log.logToErr(System.err, prefix, message, e);
	}

	public void logToErr(String message, Throwable e) {
		logToErr(null, message, e);
	}

	public void logToErr(Throwable e) {
		logToErr(null, (String) null, e);
	}

	private void onError(Throwable e) {
		if (e instanceof EOFException)
			return;

		if (e instanceof SocketException)
			return;

		e.printStackTrace();
	}

	private void onInput() {
		synchronized (this) {
			if (status == ConnectionStatus.CLOSING || status == ConnectionStatus.CLOSED)
				return;
		}

		try {
			byte[] buf = in.readEntireBlock();
			JCheckersDataInputStream block = new JCheckersDataInputStream(new ByteArrayInputStream(buf));
			tmrCheckConnection.reset();

			InputProtocol protocol = createInputProtocol(block);
			int opcode = protocol.getOpcode(block.readUChar());

			if (DebugUtil.DEBUG_MODE && DEBUG_INPUT)
				synchronized (System.out) {
					System.out.println("// Received from " + toString());
					System.out.println(DumpUtil.writeDump(buf, 0, buf.length));
					System.out.println();
				}

			switch (opcode) {
				case InputProtocol.NO_CRYPT_NEXT_BLOCK:
					in.noDecryptNextBlock();
					break;

				case InputProtocol.CHANGE_CRYPT_MODE:
					int method = in.read();
					in.setMethod(method);
					break;

				default:
					if (opcode != InputProtocol.PING_CONNECTION)
						base.parseData(opcode, block);
			}

			if (opcode != InputProtocol.NO_CRYPT_NEXT_BLOCK && opcode != InputProtocol.CHANGE_CRYPT_MODE && opcode != InputProtocol.PING_CONNECTION && opcode != InputProtocol.PONG
					&& opcode != InputProtocol.INVITATION_AUTO_REJECTED)
				lastReceivedTime = System.currentTimeMillis();
		} catch (Throwable e) {
			setError(e);
			handleException(e);
			setStatus(ConnectionStatus.CLOSED);
		}
	}

	private void onOpen() {
		try {
			socket = new Socket(host, port);
			in = new JCheckersInputStream(new BufferedInputStream(socket.getInputStream()), getGameID());
			out = new JCheckersOutputStream(new BufferedOutputStream(socket.getOutputStream()), getGameID());

			byte[] key = CryptUtil.computeKey(pcName);
			in.setKey(key);
			out.setKey(key);

			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			JCheckersDataOutputStream output = new JCheckersDataOutputStream(ba);
			OutputProtocol protocol = createOutputProtocol(output);

			output.writeUChar(protocol.getOpcode(OutputProtocol.HELLO));
			output.writeEasyString(socket.getLocalAddress().getHostName());
			output.writeEasyString(pcName);
			output.writeEasyString(username);
			output.writeEasyString(Integer.toString(roomID));
			output.writeInt(compid);

			byte[] buf = ba.toByteArray();

			if (DebugUtil.DEBUG_MODE && DEBUG_OUTPUT)
				synchronized (System.out) {
					System.out.println("// Sent to " + toString());
					System.out.println(DumpUtil.writeDump(buf, 0, buf.length));
					System.out.println();
				}

			out.noEncryptNextBlock();
			out.write(buf);
			out.flush();

			ba.reset();
			switch (mode) {
				case CREATE_TABLE:
					output.writeUChar(protocol.getOpcode(OutputProtocol.CREATE_TABLE));
					output.writeString(sid);
					params.writeTo(output);

					table = createTable();
					base = table;

					for (ConnectionListener listener : listeners)
						if (listener != null && listener instanceof TableConnectionListener)
							try {
								((TableConnectionListener) listener).onJoinTable(this, table);
							} catch (Throwable e) {
								e.printStackTrace();
							}

					break;

				case JOIN_LOBBY:
					output.writeUChar(protocol.getOpcode(OutputProtocol.JOIN_ROOM));
					output.writeString(sid);

					room = createRoom();
					base = room;

					for (ConnectionListener listener : listeners)
						if (listener != null && listener instanceof RoomConnectionListener)
							try {
								((RoomConnectionListener) listener).onJoinLobby(this, room);
							} catch (Throwable e) {
								e.printStackTrace();
							}

					break;

				case JOIN_TABLE_RECONNECTING_SITTING:
					output.writeUChar(protocol.getOpcode(OutputProtocol.JOIN_TABLE_RECONNECTED_SITING));
					output.writeString(sid);
					output.writeInt(tableID);
					output.writeUChar(sitIndex);
					output.writeInt(-1);

					table = createTable();
					base = table;

					for (ConnectionListener listener : listeners)
						if (listener != null && listener instanceof TableConnectionListener)
							try {
								((TableConnectionListener) listener).onJoinTable(this, table);
							} catch (Throwable e) {
								e.printStackTrace();
							}

					break;

				case JOIN_TABLE_RECONNECTING_WATCHING:
					output.writeUChar(protocol.getOpcode(OutputProtocol.JOIN_TABLE_RECONNECTED_WATCHING));
					output.writeString(sid);
					output.writeInt(tableID);
					output.writeUChar(0);
					output.writeInt(-1);

					table = createTable();
					base = table;

					for (ConnectionListener listener : listeners)
						if (listener != null && listener instanceof TableConnectionListener)
							try {
								((TableConnectionListener) listener).onJoinTable(this, table);
							} catch (Throwable e) {
								e.printStackTrace();
							}

					break;

				case JOIN_TABLE_SITTING:
					output.writeUChar(protocol.getOpcode(OutputProtocol.JOIN_TABLE_SITING));
					output.writeString(sid);
					output.writeInt(tableID);
					output.writeUChar(sitIndex);
					output.writeInt(-1);

					table = createTable();
					base = table;

					for (ConnectionListener listener : listeners)
						if (listener != null && listener instanceof TableConnectionListener)
							try {
								((TableConnectionListener) listener).onJoinTable(this, table);
							} catch (Throwable e) {
								e.printStackTrace();
							}

					break;

				case JOIN_TABLE_WATCHING:
					output.writeUChar(protocol.getOpcode(OutputProtocol.JOIN_TABLE_WATCHING));
					output.writeString(sid);
					output.writeInt(tableID);
					output.writeUChar(0);
					output.writeInt(0);

					table = createTable();
					base = table;

					for (ConnectionListener listener : listeners)
						if (listener != null && listener instanceof TableConnectionListener)
							try {
								((TableConnectionListener) listener).onJoinTable(this, table);
							} catch (Throwable e) {
								e.printStackTrace();
							}

					break;
			}

			buf = ba.toByteArray();

			if (DebugUtil.DEBUG_MODE && DEBUG_OUTPUT)
				synchronized (System.out) {
					System.out.println("// Sent to " + toString());
					System.out.println(DumpUtil.writeDump(buf, 0, buf.length));
					System.out.println();
				}

			out.write(buf);
			out.flush();

			tmrTestConnection.play();
			tmrCheckConnection.play();
			lastReceivedTime = System.currentTimeMillis();

			inputQueue.setName("Connection input queue [" + username + "]");
			outputQueue.setName("Connection output queue [" + username + "]");

			setStatus(ConnectionStatus.OPEN);

			inputQueue.post(() -> onInput(), 0, (e) -> handleException(e));

			for (ConnectionListener listener : listeners)
				if (listener != null)
					try {
						listener.onOpen(this);
					} catch (Throwable e) {
						e.printStackTrace();
					}
		} catch (Throwable e) {
			setError(e);
			handleException(e);
			setStatus(ConnectionStatus.CLOSED);
		}
	}

	public void post(NonReturnableProcessWithoutArg process) {
		outputQueue.post(process, (e) -> logToErr(Connection.this.toString(), null, e));
	}

	public void postAndWait(NonReturnableProcessWithoutArg process) throws InterruptedException {
		try {
			outputQueue.postAndWait(process);
		} catch (RuntimeException e) {
			logToErr(toString(), null, e);
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
			if (DebugUtil.DEBUG_MODE && DEBUG_OUTPUT) {
				OutputProtocol protocol = createOutputProtocol(null);
				if (buf[0] != protocol.getOpcode(OutputProtocol.PING_CONNECTION))
					synchronized (System.out) {
						System.out.println("// Sent to " + toString());
						System.out.println(DumpUtil.writeDump(buf, off, len));
						System.out.println();
					}
			}

			out.write(buf, off, len);
			if (flush) {
				try {
					out.flush();
				} catch (IOException e) {
					handleIOException(e);

					return;
				}
				tmrTestConnection.reset();
			} else
				out.flushBlock();
		});
	}

	public JCheckersDataOutputStream prepareOutput() {
		return new ConnectionOutputStream(new ByteArrayOutputStream());
	}

	public void removeListener(ConnectionListener listener) {
		listeners.remove(listener);
	}

	private synchronized void setError(Throwable error) {
		this.error = error;
	}

	void setMyID(int myID) {
		this.myID = myID;
	}

	private synchronized void setStatus(ConnectionStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return socket.getLocalSocketAddress() + " - " + socket.getRemoteSocketAddress();
	}

}
