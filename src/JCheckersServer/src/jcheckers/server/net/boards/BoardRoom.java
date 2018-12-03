package jcheckers.server.net.boards;

import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.server.net.Room;
import jcheckers.server.net.User;

public abstract class BoardRoom extends Room {

	protected BoardRoom() {
		super();
	}

	@Override
	public void writeUser(User user, JCheckersDataOutputStream output) {
		super.writeUser(user, output);

		BoardUser boardsUser = (BoardUser) user;
		output.writeInt(boardsUser.getRating());
	}

}
