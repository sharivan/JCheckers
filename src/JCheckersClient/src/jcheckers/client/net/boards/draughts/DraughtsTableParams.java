package jcheckers.client.net.boards.draughts;

import jcheckers.client.net.boards.BoardsTableParams;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public final class DraughtsTableParams extends BoardsTableParams {

	public static final int DRAUGHTS = 14;

	public static final int CHALLANGE = 2;

	public static final int CLASSIC = 0;
	public static final int INTERNATIONAL = 1;
	public static final int AMERICAN = 2;
	public static final int AMERICAN10x10 = 3;

	public DraughtsTableParams() {
		super();

		setGameID(14);
		setGameType(CLASSIC);
	}

	public DraughtsTableParams(JCheckersDataInputStream in) throws JCheckersIOException {
		super(in);
	}

}
