package jcheckers.client.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jcheckers.client.io.InputProtocol;
import jcheckers.client.io.OutputProtocol;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class Base {

	protected int roomID;
	protected String roomName;

	protected Connection connection;
	protected ArrayList<ConnectionListener> listeners;

	private Vector<User> users;
	private Hashtable<Integer, User> usersHash;

	protected Base(Connection connection) {
		this.connection = connection;

		listeners = connection.listeners;

		users = new Vector<>();
		usersHash = new Hashtable<>();
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

	protected OutputProtocol createOutputProtocol(JCheckersDataOutputStream output) {
		return connection.createOutputProtocol(output);
	}

	public Connection getConnection() {
		return connection;
	}

	public final int getRoomID() {
		return roomID;
	}

	public final String getRoomName() {
		return roomName;
	}

	public User getUserByID(int id) {
		return usersHash.get(id);
	}

	public User getUserByName(String name) {
		synchronized (users) {
			for (User user : users)
				if (name.equalsIgnoreCase(user.getName()))
					return user;

			return null;
		}
	}

	protected void parseData(int opcode, JCheckersDataInputStream in) throws IOException {
		switch (opcode) {
			case InputProtocol.PING:
				break;

			case InputProtocol.PONG:
				break;

			case InputProtocol.MY_ID: {
				int myID = in.readInt();
				String roomName = in.readString();

				connection.setMyID(myID);
				this.roomName = roomName;
				break;
			}

			case InputProtocol.JOIN_USER: {
				User user = connection.createUser();
				readUser(user, in);
				@SuppressWarnings("unused")
				int tableCount = in.readChar();
				@SuppressWarnings("unused")
				int bomb = in.readChar();

				users.add(user);
				usersHash.put(user.getID(), user);

				for (ConnectionListener listener : listeners)
					if (listener != null)
						try {
							listener.onJoinUser(connection, user);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.LEAVE_USER: {
				int id = in.readInt();
				User user = usersHash.remove(id);
				if (user == null)
					throw new IOException("User with id " + id + " doesn't exist in this lobby.");

				users.remove(user);

				for (ConnectionListener listener : listeners)
					if (listener != null)
						try {
							listener.onLeaveUser(connection, user);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.USER_LIST: {
				int count = in.readInt();
				User[] users = new User[count];
				for (int i = 0; i < count; i++) {
					User user = connection.createUser();
					readUser(user, in);
					@SuppressWarnings("unused")
					int tableCount = in.readChar();
					@SuppressWarnings("unused")
					int bomb = in.readChar();

					this.users.add(user);
					usersHash.put(user.getID(), user);

					users[i] = user;
				}

				for (ConnectionListener listener : listeners)
					if (listener != null)
						try {
							listener.onUserList(connection, users);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.INVALID_ROOM:
				break;

			case InputProtocol.RESPONSE_CHAT: {
				String sender = in.readEasyString();
				String message = in.readEasyString();

				for (ConnectionListener listener : listeners)
					if (listener != null)
						try {
							listener.onChat(connection, sender, message);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.RESPONSE_PRIVATE_CHAT: {
				String sender = in.readEasyString();
				String message = in.readEasyString();

				for (ConnectionListener listener : listeners)
					if (listener != null)
						try {
							listener.onPrivateChat(connection, sender, message);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.RESPONSE_ADMIN_CHAT: {
				String sender = in.readEasyString();
				String message = in.readEasyString();

				for (ConnectionListener listener : listeners)
					if (listener != null)
						try {
							listener.onAdminChat(connection, sender, message);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.NOTIFY_BANNED_AND_DISCONNECT:
				break;

			case InputProtocol.NOTIFY_BANNED_ONLY:
				break;

			case InputProtocol.RESPONSE_SERVER_VERSION:
				break;

			case InputProtocol.ACCOUNT_BLOCKED:
				break;

			case InputProtocol.SERVER_SHUTTING_DOWN:
				break;

			case InputProtocol.INVALID_PASSWORD:
				break;

			case InputProtocol.RESPONSE_INFO:
				break;

			case InputProtocol.PLAYER_NOT_FOUND_IN_SERVER:
				break;

			case InputProtocol.UPDATE_USERS:
				break;

			case InputProtocol.YOU_WAS_KICKED_BY_ADMIN:
				break;

			case InputProtocol.OTHER_WAS_KICKED_BY_ADMIN:
				break;
		}
	}

	protected void readUser(User user, JCheckersDataInputStream in) throws JCheckersIOException {
		user.id = in.readInt();
		user.name = in.readString();
	}

	public void sendMessageToChat(String message) {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = connection.createOutputProtocol(output);
		protocol.sendMessageToChat(message);
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

}
