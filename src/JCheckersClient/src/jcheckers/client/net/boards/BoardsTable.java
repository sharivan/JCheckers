package jcheckers.client.net.boards;

import jcheckers.client.net.Table;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class BoardsTable extends Table {

	protected BoardsTable(BoardsConnection connection, int roomID, int id, JCheckersDataInputStream in) throws JCheckersIOException {
		super(connection, roomID, id, in);
	}

	public int getGameType() {
		return getParams().getGameType();
	}

	@Override
	public BoardsTableParams getParams() {
		return (BoardsTableParams) super.getParams();
	}

}
