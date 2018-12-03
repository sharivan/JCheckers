package jcheckers.client.net;

public interface RoomConnectionListener extends ConnectionListener {

	void onJoinLobby(Connection c, Room room);

	void onNewTable(Connection c, Table table);

	void onRemoveTable(Connection c, Table table);

	void onUpdateTable(Connection c, Table table);

}
