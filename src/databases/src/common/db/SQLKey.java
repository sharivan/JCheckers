package common.db;

public class SQLKey {

	public static SQLKey key(String name, Object value) {
		return new SQLKey(name, value);
	}

	private String name;
	private Object value;

	private SQLKey(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

}
