package jcheckers.client.net;

public interface RoomConnectionListener extends ConnectionListener {

	void onCouldNotConnectToTheRoom(Connection c);

	void onInvited(Connection c, int tableID, int tableNumber, UserStats inviterStats);

	void onJoinLobby(Connection c, Room room);

	void onNewTable(Connection c, Table table);

	void onRemoveTable(Connection c, Table table);

	void onRoomChanged(Connection c, int roomIndex);

	void onUpdateTable(Connection c, Table table);

}
