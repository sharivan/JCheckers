package jcheckers.client.net.boards.draughts;

import jcheckers.client.net.OpenTable;
import jcheckers.client.net.Room;
import jcheckers.client.net.Table;
import jcheckers.client.net.User;
import jcheckers.client.net.boards.BoardsConnection;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public final class DraughtsConnection extends BoardsConnection {

	public DraughtsConnection(String host, int port, String username, String sid) {
		super(host, port, username, sid);
	}

	@Override
	protected Room createRoom() {
		return new DraughtsRoom(this);
	}

	@Override
	protected OpenTable createTable() {
		return new OpenDraughtsTable(this);
	}

	@Override
	protected Table createTable(int roomID, int id, JCheckersDataInputStream in) throws JCheckersIOException {
		return new DraughtsTable(this, roomID, id, in);
	}

	@Override
	protected User createUser() {
		return new DraughtsUser();
	}

}
