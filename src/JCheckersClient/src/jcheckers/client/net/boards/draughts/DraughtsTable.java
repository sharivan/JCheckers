package jcheckers.client.net.boards.draughts;

import jcheckers.client.net.TableParams;
import jcheckers.client.net.boards.BoardsTable;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public final class DraughtsTable extends BoardsTable {

	protected DraughtsTable(DraughtsConnection connection, int roomID, int id, JCheckersDataInputStream in) throws JCheckersIOException {
		super(connection, roomID, id, in);
	}

	@Override
	protected final TableParams createParams(JCheckersDataInputStream in) throws JCheckersIOException {
		return new DraughtsTableParams(in);
	}

	@Override
	public final int getMaxSeatCount() {
		return 2;
	}

	@Override
	public DraughtsTableParams getParams() {
		return (DraughtsTableParams) super.getParams();
	}

}
