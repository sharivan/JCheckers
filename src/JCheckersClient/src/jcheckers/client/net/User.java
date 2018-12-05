package jcheckers.client.net;

import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersIOException;

/**
 * 
 * Representa um usuário conectado a uma sala ou a uma mesa.
 * @author miste
 *
 */
public class User {

	int id;
	String name;
	int tableCount;
	int bomb;

	protected User() {

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (!(obj instanceof User))
			return false;

		User other = (User) obj;
		if (id != other.id)
			return false;

		return true;
	}

	public int getBomb() {
		return bomb;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getTableCount() {
		return tableCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	public boolean nameEquals(User user) {
		return name.equalsIgnoreCase(user.getName());
	}

	protected void readExtraInfo(JCheckersDataInputStream in) throws JCheckersIOException {

	}

	@Override
	public String toString() {
		return "user " + name + " (" + id + ")";
	}

}
