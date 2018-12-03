package common.db;

public class SQLExpression {

	private String value;

	public SQLExpression(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getValue();
	}

}
