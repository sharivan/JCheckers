package common.db;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class SQLConnection {

	public static final int READ = 0;
	public static final int WRITE = 1;

	private Connection connection;

	protected SQLConnection(Connection connection) {
		this.connection = connection;
	}

	public void close() {
		if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
			} finally {
				connection = null;
			}
	}

	public void commit() throws SQLException {
		connection.commit();
	}

	public boolean contains(String table, String[] keyName, Object[] keyValue) throws SQLException {
		ResultSet rs = null;
		boolean result = false;
		try {
			rs = selectAll(table, keyName, keyValue);
			result = rs != null && rs.next();
		} catch (SQLException e) {
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
		}

		return result;
	}

	protected abstract ResultSet createResultSet(ResultSet rs);

	public int delete(String table, String[] keyName, Object[] keyValue) throws SQLException {
		return executeUpdate(SQLCommand.delete(table, keyName, keyValue));
	}

	public boolean execute(String command) throws SQLException {
		Statement statement = null;
		try {
			statement = connection.prepareStatement(command);

			return statement.execute(command);
		} catch (SQLException e) {
			throw new SQLException("Exception on update: " + command, e);
		} finally {
			if (statement != null)
				statement.close();
		}
	}

	public ResultSet execute2(String update) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(update);

		try {
			statement.execute(update, Statement.RETURN_GENERATED_KEYS);
			return createResultSet(statement.getGeneratedKeys());
		} catch (SQLException e) {
			throw new SQLException("Exception on execute: " + update, e);
		}
	}

	public ResultSet executeQuery(String query) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(query);

		try {
			return createResultSet(statement.executeQuery(query));
		} catch (SQLException e) {
			throw new SQLException("Exception on query: " + query, e);
		}
	}

	public ResultSet executeQuery(String query, int resultSetType, int resultSetConcurrency) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(query, resultSetType, resultSetConcurrency);

		try {
			return createResultSet(statement.executeQuery(query));
		} catch (SQLException e) {
			throw new SQLException("Exception on query: " + query, e);
		}
	}

	public int executeUpdate(String update) throws SQLException {
		Statement statement = null;
		try {
			statement = connection.prepareStatement(update);

			return statement.executeUpdate(update);
		} catch (SQLException e) {
			throw new SQLException("Exception on update " + update, e);
		} finally {
			if (statement != null)
				statement.close();
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public ResultSet insert(String table, boolean ignore, boolean generateKeys, Object[] values) throws SQLException {
		String command = SQLCommand.insert(table, ignore, values);
		if (generateKeys)
			return execute2(command);

		execute(command);
		return null;
	}

	public ResultSet insert(String table, boolean generateKeys, Object[] values) throws SQLException {
		return insert(table, false, generateKeys, values);
	}

	public ResultSet insert(String table, Object... values) throws SQLException {
		return insert(table, false, false, values);
	}

	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	public int lastInsertID() throws SQLException {
		ResultSet rs = null;
		try {
			rs = executeQuery("SELECT LAST_INSERT_ID()");
			if (!rs.next())
				return 0;

			return rs.getInt(1);
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	public void lockTable(String table, boolean read) throws SQLException {
		execute(SQLCommand.lockTable(table, read));
	}

	public void lock(String key, String name) throws SQLException {
		execute(SQLCommand.lock(key, name));
	}
	
	public void lock(String name) throws SQLException {
		execute(SQLCommand.lock(name));
	}

	public abstract PreparedStatement prepareStatement(String query) throws SQLException;

	public abstract PreparedStatement prepareStatement(String query, int resultSetType, int resultSetConcurrency) throws SQLException;

	public void rollBack() throws SQLException {
		connection.rollback();
	}

	public ResultSet select(String table, String[] keyName, Object[] keyValue, String[] valueNames) throws SQLException {
		return executeQuery(SQLCommand.select(table, keyName, keyValue, valueNames));
	}

	public ResultSet select(String table, String[] keyName, Object[] keyValue, String[] valueNames, int resultSetType, int resultSetConcurrency) throws SQLException {
		return executeQuery(SQLCommand.select(table, keyName, keyValue, valueNames), resultSetType, resultSetConcurrency);
	}

	public ResultSet selectAll(String table) throws SQLException {
		return executeQuery(SQLCommand.selectAll(table));
	}

	public ResultSet selectAll(String table, int resultSetType, int resultSetConcurrency) throws SQLException {
		return executeQuery(SQLCommand.selectAll(table), resultSetType, resultSetConcurrency);
	}

	public ResultSet selectAll(String table, String[] keyName, Object[] keyValue) throws SQLException {
		return selectAll(table, keyName, keyValue, null);
	}

	public ResultSet selectAll(String table, String[] keyName, Object[] keyValue, int resultSetType, int resultSetConcurrency) throws SQLException {
		return selectAll(table, keyName, keyValue, null, resultSetType, resultSetConcurrency);
	}

	public ResultSet selectAll(String table, String[] keyName, Object[] keyValue, String orderBy) throws SQLException {
		return executeQuery(SQLCommand.selectAll(table, keyName, keyValue, orderBy));
	}

	public ResultSet selectAll(String table, String[] keyName, Object[] keyValue, String orderBy, int resultSetType, int resultSetConcurrency) throws SQLException {
		return executeQuery(SQLCommand.selectAll(table, keyName, keyValue, orderBy), resultSetType, resultSetConcurrency);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	public void setConnection(Connection connection) {
		if (this.connection != null)
			try {
				this.connection.close();
			} catch (SQLException e) {
			}

		this.connection = connection;
	}

	public void startTransaction() throws SQLException {
		connection.setAutoCommit(false);
	}

	public void unlockTable(String table) throws SQLException {
		execute(SQLCommand.unlockTable(table));
	}
	
	public void unlock(String name) throws SQLException {
		execute(SQLCommand.unlock(name));
	}

	public void unlock(String key, String name) throws SQLException {
		execute(SQLCommand.unlock(key, name));
	}

	public int update(String table, String[] keyName, Object[] keyValue, String[] fieldName, Object[] fieldValue) throws SQLException {
		return executeUpdate(SQLCommand.update(table, keyName, keyValue, fieldName, fieldValue));
	}

	public Blob createBlob() throws SQLException {
		return connection.createBlob();
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

}
