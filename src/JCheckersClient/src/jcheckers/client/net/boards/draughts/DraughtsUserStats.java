package jcheckers.client.net.boards.draughts;

import jcheckers.client.net.boards.BoardsUserStats;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public class DraughtsUserStats extends BoardsUserStats {

	protected DraughtsUserStats(String name, int id) {
		super(name, id);
	}

	protected DraughtsUserStats(String name, int id, JCheckersDataInputStream in) throws JCheckersIOException {
		super(name, id, in);
	}

}
