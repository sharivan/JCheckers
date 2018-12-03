package jcheckers.client.net.boards;

import jcheckers.client.net.User;

public abstract class BoardsUser extends User {

	int rating;

	protected BoardsUser() {

	}

	public int getRating() {
		return rating;
	}

}
