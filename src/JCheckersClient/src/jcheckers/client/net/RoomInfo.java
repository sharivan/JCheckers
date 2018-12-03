package jcheckers.client.net;

public class RoomInfo {

	private int id;
	private String name;
	private int users;
	private int maxUsers;
	private int tables;
	private boolean withAdmin;

	RoomInfo(int id, String name, int users, int maxUsers, int tables, boolean withAdmin) {
		this.id = id;
		this.name = name;
		this.users = users;
		this.maxUsers = maxUsers;
		this.tables = tables;
		this.withAdmin = withAdmin;
	}

	public int getID() {
		return id;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public String getName() {
		return name;
	}

	public int getTables() {
		return tables;
	}

	public int getUsers() {
		return users;
	}

	public boolean isWithAdmin() {
		return withAdmin;
	}

}
