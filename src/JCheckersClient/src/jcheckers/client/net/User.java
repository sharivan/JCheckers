package jcheckers.client.net;

public class User {

	int id;
	String name;

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

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
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

	@Override
	public String toString() {
		return "user " + name + " (" + id + ")";
	}

}
