package jcheckers.client.net;

import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class TableParams {

	public static final int PUBLIC = 0;
	public static final int PRIVATE = 1;

	private int gameID;
	private int privacy;
	private int timePerTurn;
	private boolean noWatches;

	protected TableParams() {
		gameID = 0;
		privacy = PUBLIC;
		timePerTurn = -1;
		noWatches = false;
	}

	protected TableParams(JCheckersDataInputStream in) throws JCheckersIOException {
		readFrom(in);
	}

	public int getGameID() {
		return gameID;
	}

	public int getPrivacy() {
		return privacy;
	}

	public int getTimePerTurn() {
		return timePerTurn;
	}

	public boolean isNoWatches() {
		return noWatches;
	}

	public void readFrom(JCheckersDataInputStream in) throws JCheckersIOException {
		gameID = in.readUChar();
		in.readChar();
		privacy = in.readInt();
		in.readBoolean();
		timePerTurn = in.readInt();
		noWatches = in.readBoolean();
	}

	protected void setGameID(int gameID) {
		this.gameID = gameID;
	}

	public void setNoWatches(boolean noWatches) {
		this.noWatches = noWatches;
	}

	public void setPrivacy(int privacy) {
		this.privacy = privacy;
	}

	public void setTimePerTurn(int timePerTurn) {
		this.timePerTurn = timePerTurn;
	}

	public void writeTo(JCheckersDataOutputStream out) throws JCheckersIOException {
		out.writeUChar(gameID);
		out.writeChar(0);
		out.writeInt(privacy);
		out.writeBoolean(true);
		out.writeInt(timePerTurn);
		out.writeBoolean(noWatches);
	}

}
