package jcheckers.server.net.boards.draughts;

import jcheckers.server.net.boards.BoardRoom;

public class DraughtsRoom extends BoardRoom {

	public DraughtsRoom() {
		super();
	}

	@Override
	public DraughtsServer getServer() {
		return (DraughtsServer) super.getServer();
	}

}
