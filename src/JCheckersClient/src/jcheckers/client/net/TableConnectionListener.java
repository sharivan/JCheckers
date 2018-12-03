package jcheckers.client.net;

public interface TableConnectionListener extends ConnectionListener {

	void onJoinTable(Connection c, OpenTable table);

	void onStartGame(Connection c);

	void onStopGame(Connection c);

	void onUpdate(Connection c, Table table);

}
