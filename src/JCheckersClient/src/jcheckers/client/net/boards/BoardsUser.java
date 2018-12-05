package jcheckers.client.net.boards;

import jcheckers.client.net.User;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class BoardsUser extends User {

	int rating;

	protected BoardsUser() {

	}

	public int getRating() {
		return rating;
	}

	@Override
	protected void readExtraInfo(JCheckersDataInputStream in) throws JCheckersIOException {
		rating = in.readInt();
	}

}
