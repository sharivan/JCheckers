package jcheckers.server.net.boards;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.server.io.boards.BoardOutputProtocol;
import jcheckers.server.net.User;

public class BoardUser extends User {

	private int rating;
	private int draws;
	private int streak;

	protected BoardUser() {
		super();

		rating = 1200;
		draws = 0;
		streak = 0;
	}

	public synchronized int getDraws() {
		return draws;
	}

	public synchronized int getRating() {
		return rating;
	}

	@Override
	public BoardServer getServer() {
		return (BoardServer) super.getServer();
	}

	@Override
	protected synchronized List<Object> getStats() {
		List<Object> result = super.getStats();

		result.add(draws);
		result.add(rating);
		result.add(streak);

		return result;
	}

	public synchronized int getStreak() {
		return streak;
	}

	@Override
	public BoardOutputProtocol prepareProtocol() {
		return (BoardOutputProtocol) super.prepareProtocol();
	}

	@Override
	protected synchronized void readFromStats(ResultSet rs) throws SQLException {
		super.readFromStats(rs);

		draws = rs.getInt("draws");
		rating = rs.getInt("rating");
		streak = rs.getInt("streak");
	}

	@Override
	public void writeExtraInfo(JCheckersDataOutputStream output) {
		super.writeExtraInfo(output);
		output.writeChar(0); // TODO Informar os pontos bomba aqui
	}

	@Override
	protected synchronized void writeFullProfileData1(JCheckersDataOutputStream output) {
		output.writeInt(rating);

		super.writeFullProfileData1(output);

		output.writeInt(draws);
		output.writeInt(streak);
	}

	@Override
	protected synchronized void writeProfileData1(JCheckersDataOutputStream output) {
		output.writeInt(rating);
	}

}
