package jcheckers.server.net.boards;

import java.sql.SQLException;

import common.db.SQLExpression;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.server.io.InputProtocol;
import jcheckers.server.io.OutputProtocol;
import jcheckers.server.io.boards.BoardInputProtocol;
import jcheckers.server.io.boards.BoardOutputProtocol;
import jcheckers.server.net.Server;

public abstract class BoardServer extends Server {

	private static final int MIN_RATING = 500;
	private static final int MAX_RATING = 3000;
	private static final int INITIAL_RATING = 1200;

	public static final String TABLE_BOARDS_STATS = "boards_stats";

	protected BoardServer() {
		super();
	}

	@Override
	public InputProtocol createInputProtocol(JCheckersDataInputStream input) {
		return new BoardInputProtocol(input);
	}

	@Override
	public OutputProtocol createOutputProtocol(JCheckersDataOutputStream output) {
		return new BoardOutputProtocol(output);
	}

	@Override
	public int getGameID() {
		return 0xcc;
	}

	public int getInitialRating() {
		return INITIAL_RATING;
	}

	public int getMaxRating() {
		return MAX_RATING;
	}

	public int getMinRating() {
		return MIN_RATING;
	}

	public void incrementDraws(String user) {
		incrementDraws(user, -1);
	}

	public void incrementDraws(String user, int rating) {
		try {
			executeStatsTransaction(user, (connection) -> {
				return connection.update(tableStats(), new String[] { "game", "user" }, new Object[] { getGameName(), user },
						rating != -1 ? new String[] { "playeds", "draws", "streak", "rating" } : new String[] { "playeds", "draws", "streak" },
						rating != -1 ? new Object[] { new SQLExpression("playeds + 1"), new SQLExpression("draws + 1"), 0, rating }
								: new Object[] { new SQLExpression("playeds + 1"), new SQLExpression("draws + 1"), 0 });
			});
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}
	}

	@Override
	public void incrementLosses(String user, boolean abandoned) {
		incrementLosses(user, abandoned, -1);
	}

	public void incrementLosses(String user, boolean abandoned, int rating) {
		try {
			executeStatsTransaction(user, (connection) -> {
				return connection.update(tableStats(), new String[] { "game", "user" }, new Object[] { getGameName(), user },
						rating != -1 ? new String[] { "playeds", "losses", "abandoneds", "streak", "rating" } : new String[] { "playeds", "losses", "abandoneds", "streak" },
						rating != -1
								? new Object[] { new SQLExpression("playeds + " + (abandoned ? "0" : "1")), new SQLExpression("losses + 1"),
										new SQLExpression("abandoneds + " + (abandoned ? "1" : "0")), new SQLExpression("if(streak <= -1, streak - 1, -1)"), rating }
								: new Object[] { new SQLExpression("playeds + " + (abandoned ? "0" : "1")), new SQLExpression("losses + 1"),
										new SQLExpression("abandoneds + " + (abandoned ? "1" : "0")), new SQLExpression("if(streak <= -1, streak - 1, -1)") });
			});
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}
	}

	@Override
	public void incrementWins(String user) {
		incrementWins(user, -1);
	}

	public void incrementWins(String user, int rating) {
		try {
			executeStatsTransaction(user, (connection) -> {
				return connection.update(tableStats(), new String[] { "game", "user" }, new Object[] { getGameName(), user },
						rating != -1 ? new String[] { "playeds", "wins", "streak", "rating" } : new String[] { "playeds", "wins", "streak" },
						rating != -1 ? new Object[] { new SQLExpression("playeds + 1"), new SQLExpression("wins + 1"), new SQLExpression("if(streak >= 1, streak + 1, 1)"), rating }
								: new Object[] { new SQLExpression("playeds + 1"), new SQLExpression("wins + 1"), new SQLExpression("if(streak >= 1, streak + 1, 1)") });
			});
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}
	}

	@Override
	public String tableStats() {
		return TABLE_BOARDS_STATS;
	}

	public void updateRating(String user, int rating) {
		try {
			executeStatsTransaction(user, (connection) -> {
				return connection.update(tableStats(), new String[] { "game", "user" }, new Object[] { getGameName(), user }, new String[] { "rating" }, new Object[] { rating });
			});
		} catch (SQLException e) {
			logToErr(e);
		} catch (InterruptedException e) {
			close();
		}
	}

}
