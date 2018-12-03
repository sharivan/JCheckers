package jcheckers.client.net.boards;

import jcheckers.client.net.Connection;
import jcheckers.client.net.Room;
import jcheckers.client.net.User;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class BoardsRoom extends Room {

	protected BoardsRoom(Connection connection) {
		super(connection);
	}

	@Override
	protected void readUser(User user, JCheckersDataInputStream input) throws JCheckersIOException {
		super.readUser(user, input);

		((BoardsUser) user).rating = input.readInt();
	}

}
