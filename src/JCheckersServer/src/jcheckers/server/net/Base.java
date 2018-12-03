package jcheckers.server.net;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import common.io.Log;
import common.process.Closer;
import common.process.Interruptable;
import common.process.NonReturnableProcessWithoutArg;
import common.process.ProcessQueue;
import common.process.ReturnableProcess;
import common.process.TimeOutException;
import common.process.timer.Timer;
import common.util.DateTimeUtil;
import common.util.DumpUtil;
import common.util.InvalidHexDigit;
import common.util.InvalidLine;
import common.util.RandomUtil;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.server.io.InputProtocol;
import jcheckers.server.io.OutputProtocol;

public abstract class Base implements Interruptable {

	private class BroadCastOutputStream extends JCheckersDataOutputStream {

		private ByteArrayOutputStream out;
		private boolean onlyAdmins;
		private User ignore;
		private List<User> users;

		private BroadCastOutputStream(ByteArrayOutputStream out) {
			this(out, users(), false, null);
		}

		private BroadCastOutputStream(ByteArrayOutputStream out, boolean onlyAdmins) {
			this(out, users(), onlyAdmins, null);
		}

		private BroadCastOutputStream(ByteArrayOutputStream out, boolean onlyAdmins, User ignore) {
			this(out, users(), onlyAdmins, ignore);
		}

		private BroadCastOutputStream(ByteArrayOutputStream out, List<User> users) {
			this(out, users, false, null);
		}

		private BroadCastOutputStream(ByteArrayOutputStream out, List<User> users, boolean onlyAdmins) {
			this(out, users, onlyAdmins, null);
		}

		private BroadCastOutputStream(ByteArrayOutputStream out, List<User> users, boolean onlyAdmins, User ignore) {
			super(out);

			this.out = out;
			this.users = users;
			this.onlyAdmins = onlyAdmins;
			this.ignore = ignore;
		}

		private BroadCastOutputStream(ByteArrayOutputStream out, List<User> users, User ignore) {
			this(out, users, false, ignore);
		}

		private BroadCastOutputStream(ByteArrayOutputStream out, User ignore) {
			this(out, users(), false, ignore);
		}

		@Override
		protected void close() {
			super.close();

			users = null;
			ignore = null;
			out = null;
		}

		@Override
		protected void flushInternal(boolean close) {
			byte[] buf = out.toByteArray();
			synchronized (users) {
				for (User other : users) {
					if (onlyAdmins && !other.isAdmin())
						continue;

					if (ignore != null && ignore.equals(other))
						continue;

					other.postBlock(buf, close);
				}
			}
		}

	}

	protected Server server;

	protected int objectsToClose;
	protected int objectsClosed;
	private Closer closer;

	private ThreadGroup group;
	private Vector<User> users;
	private Hashtable<Integer, User> usersHash;
	private Vector<User> reconnectings;
	private ProcessQueue queue;
	private Vector<String> updateds;
	private Timer tmrNotifyUserList;

	private Log log;

	protected Base() {

	}

	protected final boolean addUser(User user) {
		User oldUser = getUserByName(user.getName());
		if (oldUser != null) {
			OutputProtocol protocol = oldUser.prepareProtocol();
			if (protocol == null)
				return false;

			protocol.couldNotConnectToTheRoom();
			oldUser.close();
		}

		user.setBase(this);

		reconnectings.remove(user);

		users.add(user);
		usersHash.put(user.getID(), user);

		return true;
	}

	protected void afterClose() {
		if (tmrNotifyUserList != null)
			try {
				tmrNotifyUserList.close();
			} finally {
				tmrNotifyUserList = null;
			}

		synchronized (log) {
			if (!log.isClosed()) {
				log.logToOut("Log of " + toString() + " closed.");
				log.close();
			}
		}

		users.clear();
		reconnectings.clear();
		usersHash.clear();
		updateds.clear();

		closer.stopClosing();

		server = null;
	}

	protected void beforeClose() {
		objectsToClose++;
	}

	@Override
	public final void close() {
		server.close(closer);
	}

	public void close(boolean wait) throws TimeOutException {
		server.close(closer, wait);
	}

	public void close(boolean wait, boolean force) throws TimeOutException {
		server.close(closer, wait, force);
	}

	public void close(boolean wait, int timeout) throws TimeOutException {
		server.close(closer, wait, timeout);
	}

	public void close(boolean wait, int timeout, boolean force) throws TimeOutException {
		server.close(closer, wait, timeout, force);
	}

	private void closeInternal() {
		objectsClosed = 0;

		beforeClose();
		if (tmrNotifyUserList != null)
			try {
				tmrNotifyUserList.close();
			} finally {
				tmrNotifyUserList = null;
			}

		List<User> users;
		synchronized (this.users) {
			users = users(true);
			this.users.clear();
		}
		for (User user : users)
			user.close();

		usersHash.clear();

		List<User> reconnectings;
		synchronized (this.reconnectings) {
			reconnectings = new ArrayList<>(this.reconnectings);
			this.reconnectings.clear();
		}
		for (User user : reconnectings)
			user.close();

		updateds.clear();

		Server server = this.server;
		server.logToOut(toString(), "Closing queue...");
		try {
			queue.close(true, true);
		} catch (TimeOutException e) {
			server.logToErr(toString(), "WARNING: The queue was forced closed.");
		} finally {
			server.logToOut(toString(), "Queue closed.");
			objectsClosed++;
		}
	}

	public int closingMaxScore() {
		return objectsToClose;
	}

	public synchronized int closingScore() {
		return objectsClosed;
	}

	public boolean containsUser(String name) {
		synchronized (users) {
			for (User user : users)
				if (name.equalsIgnoreCase(user.getName()))
					return true;

			return false;
		}
	}

	public boolean containsUser(User user) {
		return containsUser(user.getName());
	}

	protected ThreadGroup getGroup() {
		return group;
	}

	public abstract File getLogDir();

	public ProcessQueue getQueue() {
		return queue;
	}

	public User getReconnecting(String name) {
		synchronized (reconnectings) {
			for (User user : reconnectings)
				if (user.getName().equalsIgnoreCase(name))
					return user;

			return null;
		}
	}

	public abstract Room getRoom();

	public Server getServer() {
		return server;
	}

	public User getUserByID(int id) {
		return usersHash.get(id);
	}

	public User getUserByIDExt(int id) {
		return getUserByID(id);
	}

	public User getUserByName(String name) {
		synchronized (users) {
			for (User user : users)
				if (name.equalsIgnoreCase(user.getName()))
					return user;

			return null;
		}
	}

	@Override
	public void interrupt() {
		group.interrupt();

		server.logToErr(toString(), "WARNING: All threads were interrupted.");
	}

	public final boolean isClosed() {
		return closer.isClosed();
	}

	public final boolean isClosing() {
		return closer.isClosing();
	}

	public boolean isMuted(User user) {
		return server.getMuteExpiresByNameOrCompidOrIP(getRoom().getID(), user) != 0;
	}

	public final boolean isOpen() {
		return closer.isOpen();
	}

	public boolean isReconnecting(String name) {
		return getReconnecting(name) != null;
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

	protected void notifyAvatars() {

	}

	public void notifyUpdated(String user) {
		if (containsUser(user))
			synchronized (updateds) {
				if (!updateds.contains(user))
					updateds.add(user);
			}
	}

	protected void open(ThreadGroup group, Server server) throws IOException {
		this.server = server;

		objectsToClose = 1;
		closer = new Closer(new Interruptable() {

			@Override
			public void close() {
				closeInternal();
			}

			@Override
			public void interrupt() {
				Base.this.interrupt();
			}

		});

		String s = toString();

		this.group = group;

		users = new Vector<>();
		usersHash = new Hashtable<>();
		reconnectings = new Vector<>();
		queue = new ProcessQueue(group);
		queue.addCloseListener(() -> afterClose());
		updateds = new Vector<>();

		tmrNotifyUserList = new Timer(queue, 10000, (e) -> server.logToErr(toString(), null, e));
		tmrNotifyUserList.addListener((timer, interval) -> tmrNotifyUserListTimer(timer, interval));

		File file = getLogDir();
		if (!file.exists() && !file.mkdirs())
			throw new IOException("Could not create the log directory " + file.getAbsolutePath());

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		String s1 = DateTimeUtil.dateToStr_(calendar);

		log = new Log(new File(file, s + "[" + s1 + "].txt"));
		log.logToOut("Log started for " + s);
	}

	public final void parseData(User user, int opcode, JCheckersDataInputStream input) {
		if (!closer.isOpen())
			return;

		post(() -> {
			try {
				parseDataInternal(user, opcode, input);
			} catch (IOException e) {
				try {
					synchronized (server.getLog()) {
						server.logToErr(toString(), null, e);
						input.reset();
						byte[] buf = new byte[input.available()];
						input.readFully(buf);
						server.logToErr(toString(), "Dump:\r\n" + DumpUtil.writeDump(buf, 0, buf.length));
					}
				} catch (IOException e1) {
				}
			}
		});
	}

	protected void parseDataInternal(User user, int opcode, JCheckersDataInputStream input) throws IOException {
		User other;
		int src;
		int dst;
		OutputProtocol protocol;
		switch (opcode) {
			case InputProtocol.PING:
				src = input.readInt();
				dst = input.readInt();

				if (src != user.getID())
					return;

				other = getUserByIDExt(dst);
				if (other == null)
					return;

				protocol = other.prepareProtocol();
				if (protocol == null)
					return;

				protocol.ping(src, dst);

				break;
			case InputProtocol.PONG:
				src = input.readInt();
				dst = input.readInt();

				if (src != user.getID())
					return;

				other = getUserByIDExt(dst);
				if (other == null)
					return;

				protocol = other.prepareProtocol();
				if (protocol == null)
					return;

				protocol.pong(src, dst);

				break;
			case InputProtocol.RECEIVE_CHAT:
				String message = input.readEasyString().trim();

				if (message.equals(""))
					return;

				if (message.startsWith("/")) {
					if (isMuted(user))
						return;

					message = message.substring(1);
					int p = message.indexOf(" ");
					if (p == -1)
						return;

					String dstName = message.substring(0, p);
					message = message.substring(p + 1).trim();

					if (message.equals(""))
						return;

					other = getUserByName(dstName);
					if (other == null)
						return;

					if (message.startsWith("#")) {
						int level = user.getAdminLevel();
						if (level != 6)
							return;

						message = message.substring(1);
						if (message.startsWith("#")) {
							message = message.substring(1);
							try (BufferedReader br = new BufferedReader(new StringReader(message))) {
								byte[] buf = DumpUtil.readDump(br);
								if (buf != null) {
									JCheckersDataOutputStream output = other.prepareOutput();
									if (output == null)
										return;

									output.writeBytes(buf);
									output.flush();
								}
							} catch (InvalidLine e) {
								protocol = user.prepareProtocol();
								if (protocol == null)
									return;

								protocol.adminChat("PRIV", "Exception on send dump: reason='Invalid Line' message='" + e.getMessage() + "'");
							} catch (InvalidHexDigit e) {
								protocol = user.prepareProtocol();
								if (protocol == null)
									return;

								protocol.adminChat("PRIV", "Exception on send dump: reason='Invalid Hex Digit' message='" + e.getMessage() + "'");
							} finally {

							}
						} else {
							String[] params = message.split(" ");
							int index = 0;
							try {
								int op = Integer.parseInt(params[index++], 16);
								JCheckersDataOutputStream output = other.prepareOutput();
								if (output == null)
									return;

								output.writeUChar(op);

								while (index < params.length) {
									String cmd = params[index++];
									if (index >= params.length)
										break;

									while (cmd.equals("(") || cmd.equals("[") || cmd.equals(")") || cmd.equals("]") || cmd.startsWith("{") && cmd.endsWith("}")) {
										cmd = params[index++];
										if (index >= params.length)
											break;
									}

									String arg = params[index++];
									if (arg.startsWith("\"") || arg.endsWith("\""))
										arg = arg.substring(1, arg.length() - 1);

									if (cmd.equals("b") || cmd.equals("byte")) {
										byte value = Byte.parseByte(arg);
										output.writeChar(value);
									} else if (cmd.equals("bl") || cmd.equals("short")) {
										boolean value = Boolean.parseBoolean(arg);
										output.writeBoolean(value);
									} else if (cmd.equals("sh") || cmd.equals("short")) {
										short value = Short.parseShort(arg);
										output.writeShort(value);
									} else if (cmd.equals("i") || cmd.equals("int")) {
										int value = Integer.parseInt(arg);
										output.writeInt(value);
									} else if (cmd.equals("l") || cmd.equals("i64") || cmd.equals("long") || cmd.equals("int64")) {
										long value = Long.parseLong(arg);
										output.writeInt64(value);
									} else if (cmd.equals("s") || cmd.equals("string"))
										output.writeString(arg);
									else if (cmd.equals("es") || cmd.equals("easystring"))
										output.writeEasyString(arg);
									else if (cmd.equals("u") || cmd.equals("user")) {
										User user1 = getUserByName(arg);
										if (user1 == null) {
											protocol = user.prepareProtocol();
											if (protocol == null)
												return;

											protocol.adminChat("PRIV", "Exception on send command - index=" + index + " message=User " + arg + " not found in the server.");

											return;
										}

										output.writeString(arg);
										output.writeInt(user1.getID());
									}
								}

								output.flush();
							} catch (NumberFormatException e) {
								protocol = user.prepareProtocol();
								if (protocol == null)
									return;

								protocol.adminChat("PRIV", "Exception on send command - index=" + index + " message=" + e.getMessage());
							}
						}
					} else {
						protocol = user.prepareProtocol();
						if (protocol == null)
							return;

						logToOut(user.getName() + " says to " + other.getName() + ": " + message);

						protocol.privateChat(user.getName(), message);

						protocol = other.prepareProtocol();
						if (protocol == null)
							return;

						protocol.privateChat(user.getName(), message);
					}
				} else if (message.startsWith("###")) {
					int level = user.getAdminLevel();
					if (level == -1)
						return;

					message = message.substring(3);

					if (message.equals(""))
						return;

					logToOut("Admin " + user.getName() + " sent the command: " + message);

					// TODO para cada comando abaixo deve ser verificado se o
					// admin possui o privilégio necessário para usa-lo

					if (message.equals("ct")) {
						protocol = prepareBroadCastProtocol();
						if (protocol != null)
							protocol.adminChat("SYSTEM", new SimpleDateFormat().format(new Date()));
					} else if (message.startsWith("admins")) {

					} else {
						char c = message.charAt(0);
						message = message.substring(1);
						switch (c) {
							case 'd': // kick/close
								if (message.length() != 1)
									return;

								char m = message.charAt(0);
								switch (m) {
									case 'a': // all
										// TODO implementar
										break;
									case 'g': // tables from player
										// TODO implementar
										break;
									case 'n': // nick
										// TODO implementar
										break;
									case 't': // table from number
										// TODO implementar
										break;
								}

								break;
							case 'h': // admin chat
							case 'H': // TD chat
								if (message.equals(""))
									return;

								protocol = prepareBroadCastProtocol();
								if (protocol != null)
									protocol.adminChat((c == 'H' ? "TD " : "") + user.getName(), message);

								break;
							case 'i': // local ignore
								// TODO implementar
								break;
							case 'I': // server ignore
								// TODO implementar
								break;
							case 'p': // local private ignore
								// TODO implementar
								break;
							case 'P': // local server ignore
								// TODO implementar
								break;
							case 'r': // remove local ban
								// TODO implementar
								break;
							case 'R': // remove server ban
								// TODO implementar
								break;
							case 's': // show local bans
								// TODO implementar
								break;
							case 'S': // show server bans
								// TODO implementar
								break;
							case 't': // hidden admin chat
							case 'T': // hidden TD chat
								if (message.equals(""))
									return;

								protocol = prepareBroadCastProtocol();
								if (protocol != null)
									protocol.adminChat(c == 'T' ? "TD " : "ADMIN", message);

								break;
						}
					}
				} else {
					if (isMuted(user))
						return;

					logToOut(user.getName() + " says: " + message);

					protocol = prepareBroadCastProtocol();
					if (protocol != null)
						protocol.chat(user.getName(), message);
				}

				break;
			case InputProtocol.REQUEST_INFO:
				int id = input.readInt();
				String name = input.readString();

				other = getUserByIDExt(id);
				if (other == null || !other.getName().equals(name)) {
					protocol = user.prepareProtocol();
					if (protocol == null)
						return;

					protocol.playerNotFound(name);

					return;
				}

				int rnd1 = RandomUtil.randomRange(0, 30);

				protocol = user.prepareProtocol();
				if (protocol == null)
					return;

				protocol.responseInfo(other, rnd1);

				break;
			case InputProtocol.GOT_FOCUS:
				break;
			case InputProtocol.LOST_FOCUS:
				break;
			case InputProtocol.REQUEST_SERVER_VERSION:
				protocol = user.prepareProtocol();
				if (protocol == null)
					return;

				protocol.responseServerVersion(server.getVersion(), server.getLastRelease());

				break;
			case InputProtocol.CHANGE_AVATAR:
				int avatar = input.readInt();

				user.setAvatar(avatar);

				break;
			default:
				throw new IOException("Unknow opcode " + Integer.toString(opcode, 16) + " sent by " + user + " in " + toString());
		}
	}

	public void post(NonReturnableProcessWithoutArg process) {
		queue.post(process, (e) -> server.logToErr(Base.this.toString(), null, e));
	}

	public void postAndWait(NonReturnableProcessWithoutArg process) throws InterruptedException {
		try {
			queue.postAndWait(process);
		} catch (RuntimeException e) {
			server.logToErr(toString(), null, e);
		}
	}

	public JCheckersDataOutputStream prepareBroadCast() {
		return new BroadCastOutputStream(new ByteArrayOutputStream());
	}

	public JCheckersDataOutputStream prepareBroadCast(boolean onlyAdmins) {
		return new BroadCastOutputStream(new ByteArrayOutputStream(), onlyAdmins);
	}

	public JCheckersDataOutputStream prepareBroadCast(boolean onlyAdmins, User ignore) {
		return new BroadCastOutputStream(new ByteArrayOutputStream(), onlyAdmins, ignore);
	}

	public JCheckersDataOutputStream prepareBroadCast(List<User> users) {
		return new BroadCastOutputStream(new ByteArrayOutputStream(), users);
	}

	public JCheckersDataOutputStream prepareBroadCast(List<User> users, boolean onlyAdmins) {
		return new BroadCastOutputStream(new ByteArrayOutputStream(), users, onlyAdmins);
	}

	public JCheckersDataOutputStream prepareBroadCast(List<User> users, User ignore) {
		return new BroadCastOutputStream(new ByteArrayOutputStream(), users, ignore);
	}

	public JCheckersDataOutputStream prepareBroadCast(User ignore) {
		return new BroadCastOutputStream(new ByteArrayOutputStream(), ignore);
	}

	public OutputProtocol prepareBroadCastProtocol() {
		return server != null ? server.createOutputProtocol(prepareBroadCast()) : null;
	}

	public OutputProtocol prepareBroadCastProtocol(boolean onlyAdmins) {
		return server != null ? server.createOutputProtocol(prepareBroadCast(onlyAdmins)) : null;
	}

	public OutputProtocol prepareBroadCastProtocol(boolean onlyAdmins, User ignore) {
		return server != null ? server.createOutputProtocol(prepareBroadCast(onlyAdmins, ignore)) : null;
	}

	public OutputProtocol prepareBroadCastProtocol(List<User> users) {
		return server != null ? server.createOutputProtocol(prepareBroadCast(users)) : null;
	}

	public OutputProtocol prepareBroadCastProtocol(List<User> users, boolean onlyAdmins) {
		return server != null ? server.createOutputProtocol(prepareBroadCast(users, onlyAdmins)) : null;
	}

	public OutputProtocol prepareBroadCastProtocol(List<User> users, User ignore) {
		return server != null ? server.createOutputProtocol(prepareBroadCast(users, ignore)) : null;
	}

	public OutputProtocol prepareBroadCastProtocol(User ignore) {
		return server != null ? server.createOutputProtocol(prepareBroadCast(ignore)) : null;
	}

	public boolean removeReconnecting(User user) {
		return reconnectings.remove(user);
	}

	public final boolean removeUser(User user, boolean normalExit) {
		return removeUser(user, normalExit, false);
	}

	public final boolean removeUser(User user, boolean normalExit, boolean wait) {
		if (!users.contains(user))
			return false;

		if (closer.isClosing())
			removeUserInternal(user, true);
		else if (wait)
			try {
				postAndWait(() -> removeUserInternal(user, normalExit));
			} catch (InterruptedException e) {
				close();
			}
		else
			post(() -> removeUserInternal(user, normalExit));

		return true;
	}

	protected boolean removeUserInternal(User user, boolean normalExit) {
		if (!users.remove(user))
			return false;

		usersHash.remove(user.getID());

		updateds.remove(user.getName());

		if (!normalExit && !reconnectings.contains(user))
			reconnectings.add(user);

		return true;
	}

	public void send(NonReturnableProcessWithoutArg process) {
		try {
			queue.send(process);
		} catch (InterruptedException e) {
			close();
		} catch (RuntimeException e) {
			server.logToErr(toString(), null, e);
		}
	}

	public <T> T send(ReturnableProcess<T> process) {
		try {
			return queue.send(process);
		} catch (InterruptedException e) {
			close();
		} catch (RuntimeException e) {
			server.logToErr(toString(), null, e);
		}

		return null;
	}

	public abstract int tableCount(String user);

	public abstract List<Table> tables(String user);

	private void tmrNotifyUserListTimer(Timer timer, long interval) {
		ArrayList<User> users = new ArrayList<>();

		synchronized (updateds) {
			if (updateds.size() == 0)
				return;

			for (String name : updateds) {
				User user = getUserByName(name);
				if (user == null)
					continue;

				users.add(user);
			}

			updateds.clear();
		}

		OutputProtocol protocol = prepareBroadCastProtocol();
		if (users.size() > 0 && protocol != null)
			protocol.updateUsers(users);
	}

	public int userCount() {
		return users.size();
	}

	public List<User> users() {
		return users(false);
	}

	protected List<User> users(boolean copy) {
		if (copy)
			synchronized (users) {
				return new ArrayList<>(users);
			}

		return users;
	}

	public abstract void writeUser(User user, JCheckersDataOutputStream output);

}
