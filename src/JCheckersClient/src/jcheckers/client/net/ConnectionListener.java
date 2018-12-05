package jcheckers.client.net;

public interface ConnectionListener {

	void onAdminChat(Connection connection, String sender, String message);

	void onBanned(Connection c, String bannedUntil);

	void onChat(Connection c, String sender, String message);

	void onClose(Connection c);

	void onError(Connection c, Throwable e);

	void onInvalidPassword(Connection c);

	void onInvalidRoom(Connection c);

	void onJoinUser(Connection c, User user);

	void onLeaveUser(Connection c, User user);

	void onOpen(Connection c);

	void onPlayerNotFoundInServer(Connection c, String name);

	void onPong(Connection c, int src, int dst);

	void onPrivateChat(Connection c, String sender, String message);

	void onResponseInfo(Connection c, UserStats stats, int[] tables, int inactiveTime);

	void onServerShuttingDown(Connection c);

	void onServerVersion(Connection c, String version, String lastRelease);

	void onUpdateUsers(Connection c, User[] users);

	void onUserList(Connection c, User[] users);

	void onWelcome(Connection c, String roomName);

}
