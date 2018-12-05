package jcheckers.client.net.boards;

import jcheckers.client.net.UserStats;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class BoardsUserStats extends UserStats {

	private int draws;
	private int rating;
	private int streak;

	protected BoardsUserStats(String name, int id) {
		super(name, id);
	}

	protected BoardsUserStats(String name, int id, JCheckersDataInputStream in) throws JCheckersIOException {
		super(name, id, in);
	}

	public int getDraws() {
		return draws;
	}

	public int getRating() {
		return rating;
	}

	public int getStreak() {
		return streak;
	}

	@Override
	protected void read(JCheckersDataInputStream in) throws JCheckersIOException {
		rating = in.readInt();

		super.read(in);

		draws = in.readInt();
		streak = in.readInt();
	}

}
