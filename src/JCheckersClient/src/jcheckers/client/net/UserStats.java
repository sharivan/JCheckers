package jcheckers.client.net;

import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

/**
 * 
 * Representa as estatísticas de um usuário, contendo informações como o número total de partidas jogadas, vítórias, derrotas, etc.
 * @author miste
 *
 */
public abstract class UserStats {

	private String name;
	private int id;

	private int totalGames;
	private int abandoneds;
	private int wins;
	private int losses;

	protected UserStats(String name, int id) {
		this.name = name;
		this.id = id;
	}

	protected UserStats(String name, int id, JCheckersDataInputStream in) throws JCheckersIOException {
		this(name, id);

		read(in);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserStats))
			return false;
		UserStats other = (UserStats) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getAbandoneds() {
		return abandoneds;
	}

	public int getID() {
		return id;
	}

	public int getLosses() {
		return losses;
	}

	public String getName() {
		return name;
	}

	public int getTotalGames() {
		return totalGames;
	}

	public int getWins() {
		return wins;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	protected void read(JCheckersDataInputStream in) throws JCheckersIOException {
		totalGames = in.readInt();
		abandoneds = in.readInt();
		wins = in.readInt();
		losses = in.readInt();
	}

}
