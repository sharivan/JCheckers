package jcheckers.server.net.boards.draughts;

import jcheckers.server.net.Room;
import jcheckers.server.net.Table;
import jcheckers.server.net.User;
import jcheckers.server.net.boards.BoardServer;

public class DraughtsServer extends BoardServer {

	private static final String VERSION = "1.0";
	private static final String LAST_RELEASE = "15/10/2018 16:08";

	@Override
	public String getGameName() {
		return "draughts";
	}

	@Override
	public String getLastRelease() {
		return LAST_RELEASE;
	}

	@Override
	protected Class<? extends Room> getRoomClass() {
		return DraughtsRoom.class;
	}

	@Override
	protected Class<? extends Table> getTableClass() {
		return DraughtsTable.class;
	}

	@Override
	protected Class<? extends User> getUserClass() {
		return DraughtsUser.class;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

}
