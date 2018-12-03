package jcheckers.client.net;

public interface ConnectionListener {

	void onAdminChat(Connection connection, String sender, String message);

	void onChat(Connection c, String sender, String message);

	void onClose(Connection c);

	void onError(Connection c, Throwable e);

	void onJoinUser(Connection c, User user);

	void onLeaveUser(Connection c, User user);

	void onOpen(Connection c);

	void onPrivateChat(Connection c, String sender, String message);

	void onUserList(Connection connection, User[] users);

}
