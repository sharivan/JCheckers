package jcheckers.client.net.boards;

import jcheckers.client.net.TableParams;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class BoardsTableParams extends TableParams {

	private int gameType;
	private boolean rated;
	private boolean hasRatingLimit;
	private int minRating;
	private int maxRating;

	private boolean hasTimePerTurn;
	private boolean hasIncrementTime;
	private int incrementTime;
	private boolean hasTime;
	private int time;

	private boolean activeHelp;
	private boolean swapSides;

	protected BoardsTableParams() {
		super();

		gameType = 0;
		rated = true;
		hasRatingLimit = false;
		minRating = 0;
		maxRating = 3000;

		hasTimePerTurn = false;
		hasIncrementTime = false;
		incrementTime = 0;
		hasTime = true;
		time = 10;

		activeHelp = false;
		swapSides = true;
	}

	public BoardsTableParams(JCheckersDataInputStream in) throws JCheckersIOException {
		super(in);
	}

	public int getGameType() {
		return gameType;
	}

	public int getIncrementTime() {
		return incrementTime;
	}

	public int getMaxRating() {
		return maxRating;
	}

	public int getMinRating() {
		return minRating;
	}

	public int getTime() {
		return time;
	}

	public boolean hasIncrementTime() {
		return hasIncrementTime;
	}

	public boolean hasRatingLimit() {
		return hasRatingLimit;
	}

	public boolean hasTime() {
		return hasTime;
	}

	public boolean hasTimePerTurn() {
		return hasTimePerTurn;
	}

	public boolean isActiveHelp() {
		return activeHelp;
	}

	public boolean isRated() {
		return rated;
	}

	public boolean isSwapSides() {
		return swapSides;
	}

	@Override
	public void readFrom(JCheckersDataInputStream in) throws JCheckersIOException {
		setGameID(in.readUChar());
		gameType = in.readUChar();
		setPrivacy(in.readUChar());
		rated = in.readBoolean();
		hasRatingLimit = in.readBoolean();
		minRating = in.readInt();
		maxRating = in.readInt();

		hasTimePerTurn = in.readBoolean();
		setTimePerTurn(in.readInt());
		hasIncrementTime = in.readBoolean();
		incrementTime = in.readInt();
		hasTime = in.readBoolean();
		time = in.readInt();

		int flags = in.readUChar();
		setNoWatches((flags & 1) != 0);
		in.readUChar();
		in.readInt64();
		flags = in.readUChar();
		activeHelp = (flags & 1) != 0;
		swapSides = (flags & 2) != 0;

		in.readInt();
		in.readInt();
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public void setHasIncrementTime(boolean hasIncrementTime) {
		this.hasIncrementTime = hasIncrementTime;
	}

	public void setHasRatingLimit(boolean hasRatingLimit) {
		this.hasRatingLimit = hasRatingLimit;
	}

	public void setHasTime(boolean hasTime) {
		this.hasTime = hasTime;
	}

	public void setHasTimePerTurn(boolean hasTimePerTurn) {
		this.hasTimePerTurn = hasTimePerTurn;
	}

	public void setIncrementTime(int incrementTime) {
		this.incrementTime = incrementTime;
	}

	public void setMaxRating(int maxRating) {
		this.maxRating = maxRating;
	}

	public void setMinRating(int minRating) {
		this.minRating = minRating;
	}

	public void setRated(boolean rated) {
		this.rated = rated;
	}

	public void setSwapSides(boolean swapSides) {
		this.swapSides = swapSides;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public void writeTo(JCheckersDataOutputStream out) throws JCheckersIOException {
		out.writeChar(getGameID());
		out.writeChar(gameType);
		out.writeChar(getPrivacy());
		out.writeBoolean(rated);
		out.writeBoolean(hasRatingLimit);
		out.writeInt(minRating);
		out.writeInt(maxRating);

		out.writeBoolean(hasTimePerTurn);
		out.writeInt(getTimePerTurn());
		out.writeBoolean(hasIncrementTime);
		out.writeInt(incrementTime);
		out.writeBoolean(hasTime);
		out.writeInt(time);

		out.writeChar((isNoWatches() ? 1 : 0) | 4); // flags1
													// (byte1=escape=pausa
													// byte2=sem visitantes)
		out.writeChar(0);
		out.writeInt64(1);
		out.writeChar((activeHelp ? 1 : 0) | (swapSides ? 2 : 0)); // flags2
																	// (byte1=ajuda
																	// ativa
																	// byte2=mudar
																	// de
																	// lado)
		out.writeInt(3);
		out.writeInt(1);
	}

}
