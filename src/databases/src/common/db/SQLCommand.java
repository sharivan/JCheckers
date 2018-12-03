package common.db;

public class SQLCommand {

	public static String delete(String table, String[] keyName, Object[] keyValue) {
		String command = "DELETE FROM " + table + " WHERE " + keyName[0] + (keyValue[0] instanceof IsNull ? " IS NULL" : " = " + SQLFormater.formatValue(keyValue[0]));
		for (int i = 1; i < keyName.length; i++)
			command += " AND " + keyName[i] + (keyValue[i] instanceof IsNull ? " IS NULL" : " = " + SQLFormater.formatValue(keyValue[i]));

		return command;
	}

	public static String insert(String table, boolean ignore, Object[] values) {
		final String command = "INSERT " + (ignore ? "IGNORE " : "") + "INTO " + table + " VALUES (" + SQLFormater.formatValues(values) + ")";

		return command;
	}

	public static String insert(String table, Object[] values) {
		return insert(table, false, values);
	}

	public static String lockTable(String table, boolean read) {
		return "LOCK TABLES " + table + " " + (read ? "READ" : "WRITE");
	}

	public static String lock(String key, String name) {
		return lock(key + " " + name);
	}
	
	public static String lock(String name) {
		return "DO GET_LOCK(\"" + name + "\", 32767)";
	}

	public static String select(String table, String[] keyName, Object[] keyValue, String[] valueNames) {
		String vns = valueNames[0];
		for (int i = 1; i < valueNames.length; i++)
			vns += ", " + valueNames[i];
		String command = "SELECT " + vns + " FROM " + table + " WHERE " + keyName[0] + (keyValue[0] instanceof IsNull ? " IS NULL" : " = " + SQLFormater.formatValue(keyValue[0]));
		for (int i = 1; i < keyName.length; i++)
			command += " AND " + keyName[i] + (keyValue[i] instanceof IsNull ? " IS NULL" : " = " + SQLFormater.formatValue(keyValue[i]));

		return command;
	}

	public static String selectAll(String table) {
		return "SELECT * FROM " + table;
	}

	public static String selectAll(String table, String[] keyName, Object[] keyValue) {
		return selectAll(table, keyName, keyValue, null);
	}
	
	public static String selectAll(String table, String[] keyName, Object[] keyValue, String orderBy) {
		return selectAll(table, keyName, keyValue, orderBy, false);
	}

	public static String selectAll(String table, String[] keyName, Object[] keyValue, String orderBy, boolean desc) {
		String command = "SELECT * FROM " + table + " WHERE " + keyName[0] + (keyValue[0] instanceof IsNull ? " IS NULL" : " = " + SQLFormater.formatValue(keyValue[0]));
		for (int i = 1; i < keyName.length; i++)
			command += " AND " + keyName[i] + (keyValue[i] instanceof IsNull ? " IS NULL" : " = " + SQLFormater.formatValue(keyValue[i]));
		
		if (orderBy != null) {
			command += " ORDER BY " + orderBy;
			if (desc)
				command += " DESC";
		}

		return command;
	}

	public static String unlockTable(String table) {
		return "UNLOCK TABLES " + table;
	}
	
	public static String unlock(String name) {
		return "DO RELEASE_LOCK(\"" + name + "\")";
	}

	public static String unlock(String key, String name) {
		return unlock(key + " " + name);
	}

	public static String update(String table, String[] keyName, Object[] keyValue, String[] fieldName, Object[] fieldValue) {
		String command = "UPDATE " + table + " SET " + fieldName[0] + " = " + SQLFormater.formatValue(fieldValue[0]);
		for (int i = 1; i < fieldName.length; i++)
			command += ", " + fieldName[i] + " = " + SQLFormater.formatValue(fieldValue[i]);
		command += " WHERE " + keyName[0] + (keyValue[0] instanceof IsNull ? " IS NULL" : " = " + SQLFormater.formatValue(keyValue[0]));
		for (int i = 1; i < keyName.length; i++)
			command += " AND " + keyName[i] + (keyValue[i] instanceof IsNull ? " IS NULL" : " = " + SQLFormater.formatValue(keyValue[i]));

		return command;
	}

	private SQLCommand() {

	}

}
