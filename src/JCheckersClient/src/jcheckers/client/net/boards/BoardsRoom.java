package jcheckers.client.net.boards;

import jcheckers.client.net.Connection;
import jcheckers.client.net.Room;

public abstract class BoardsRoom extends Room {

	protected BoardsRoom(Connection connection) {
		super(connection);
	}

}
