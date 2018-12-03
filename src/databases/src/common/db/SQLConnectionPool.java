/**
 * 
 */
package common.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author saddam
 * 
 */
public class SQLConnectionPool implements AutoCloseable {

	private class SQLConnectionPoolConnection extends SQLConnection {

		private boolean releaseOnCompletion;

		private Thread owner;

		protected SQLConnectionPoolConnection(Connection connection) {
			super(connection);
		}

		private synchronized void acquire(boolean releaseOnCompletion) {
			owner = Thread.currentThread();

			this.releaseOnCompletion = releaseOnCompletion;
		}

		@Override
		public void close() {
			release();
		}

		private void closeInternal() {
			super.close();
		}

		@Override
		public void commit() throws SQLException {
			ensureValid();

			super.commit();

			if (releaseOnCompletion)
				release();
		}

		@Override
		protected ResultSet createResultSet(ResultSet rs) {
			return new SQLConnectionPoolResultSet(this, rs, releaseOnCompletion);
		}

		protected synchronized void ensureValid() {
			if (owner != Thread.currentThread())
				throw new RuntimeException("This thread doesn't own this connection.");
		}

		@Override
		public boolean execute(String command) throws SQLException {
			ensureValid();

			try {
				return super.execute(command);
			} finally {
				if (releaseOnCompletion)
					release();
			}
		}

		@Override
		public ResultSet execute2(String command) throws SQLException {
			ensureValid();

			return super.execute2(command);
		}

		@Override
		public ResultSet executeQuery(String query) throws SQLException {
			ensureValid();

			return super.executeQuery(query);
		}

		@Override
		public ResultSet executeQuery(String query, int resultSetType, int resultSetConcurrency) throws SQLException {
			ensureValid();

			return super.executeQuery(query, resultSetType, resultSetConcurrency);
		}

		@Override
		public int executeUpdate(String update) throws SQLException {
			ensureValid();

			try {
				return super.executeUpdate(update);
			} finally {
				if (releaseOnCompletion)
					release();
			}
		}

		private synchronized boolean isUsing() {
			return owner != null;
		}

		@Override
		public PreparedStatement prepareStatement(String query) throws SQLException {
			return new SQLConnectionPoolPreparedStatement(this, getConnection().prepareStatement(query), releaseOnCompletion);
		}

		@Override
		public PreparedStatement prepareStatement(String query, int resultSetType, int resultSetConcurrency) throws SQLException {
			return new SQLConnectionPoolPreparedStatement(this, getConnection().prepareStatement(query, resultSetType, resultSetConcurrency), releaseOnCompletion);
		}

		private void release() {
			synchronized (this) {
				if (owner == null || owner != Thread.currentThread())
					return;

				owner = null;
			}

			semaphore.release();
		}

		public void reOpen(boolean autoCommit) throws SQLException, InterruptedException {
			setConnection(createNewConnection(autoCommit));
		}

		@Override
		public void rollBack() throws SQLException {
			ensureValid();

			super.rollBack();

			if (releaseOnCompletion)
				release();
		}

		@Override
		public void startTransaction() throws SQLException {
			ensureValid();

			super.startTransaction();
		}

	}

	private class SQLConnectionPoolPreparedStatement implements PreparedStatement {

		private SQLConnectionPoolConnection connection;
		private PreparedStatement ps;
		private boolean releaseOnCompletion;

		private SQLConnectionPoolPreparedStatement(SQLConnectionPoolConnection connection, PreparedStatement ps, boolean releaseOnCompletion) {
			this.connection = connection;
			this.ps = ps;
			this.releaseOnCompletion = releaseOnCompletion;
		}

		/**
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#addBatch()
		 */
		@Override
		public void addBatch() throws SQLException {
			ps.addBatch();
		}

		/**
		 * @param sql
		 * @throws SQLException
		 * @see java.sql.Statement#addBatch(java.lang.String)
		 */
		@Override
		public void addBatch(String sql) throws SQLException {
			ps.addBatch(sql);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#cancel()
		 */
		@Override
		public void cancel() throws SQLException {
			ps.cancel();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#clearBatch()
		 */
		@Override
		public void clearBatch() throws SQLException {
			ps.clearBatch();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#clearParameters()
		 */
		@Override
		public void clearParameters() throws SQLException {
			ps.clearParameters();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#clearWarnings()
		 */
		@Override
		public void clearWarnings() throws SQLException {
			ps.clearWarnings();
		}

		@Override
		public void close() {
			try {
				ps.close();
			} catch (SQLException e) {
			} finally {
				ps = null;
				if (connection != null) {
					if (releaseOnCompletion)
						connection.release();
					connection = null;
				}
			}
		}

		@Override
		public void closeOnCompletion() throws SQLException {
			ps.closeOnCompletion();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#execute()
		 */
		@Override
		public boolean execute() throws SQLException {
			return ps.execute();
		}

		/**
		 * @param sql
		 * @throws SQLException
		 * @see java.sql.Statement#execute(java.lang.String)
		 */
		@Override
		public boolean execute(String sql) throws SQLException {
			return ps.execute(sql);
		}

		/**
		 * @param sql
		 * @param autoGeneratedKeys
		 * @throws SQLException
		 * @see java.sql.Statement#execute(java.lang.String, int)
		 */
		@Override
		public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
			return ps.execute(sql, autoGeneratedKeys);
		}

		/**
		 * @param sql
		 * @param columnIndexes
		 * @throws SQLException
		 * @see java.sql.Statement#execute(java.lang.String, int[])
		 */
		@Override
		public boolean execute(String sql, int[] columnIndexes) throws SQLException {
			return ps.execute(sql, columnIndexes);
		}

		/**
		 * @param sql
		 * @param columnNames
		 * @throws SQLException
		 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
		 */
		@Override
		public boolean execute(String sql, String[] columnNames) throws SQLException {
			return ps.execute(sql, columnNames);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#executeBatch()
		 */
		@Override
		public int[] executeBatch() throws SQLException {
			return ps.executeBatch();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#executeQuery()
		 */
		@Override
		public SQLConnectionPoolResultSet executeQuery() throws SQLException {
			return new SQLConnectionPoolResultSet(connection, ps.executeQuery(), releaseOnCompletion);
		}

		/**
		 * @param sql
		 * @throws SQLException
		 * @see java.sql.Statement#executeQuery(java.lang.String)
		 */
		@Override
		public SQLConnectionPoolResultSet executeQuery(String sql) throws SQLException {
			return new SQLConnectionPoolResultSet(connection, ps.executeQuery(sql), releaseOnCompletion);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#executeUpdate()
		 */
		@Override
		public int executeUpdate() throws SQLException {
			return ps.executeUpdate();
		}

		/**
		 * @param sql
		 * @throws SQLException
		 * @see java.sql.Statement#executeUpdate(java.lang.String)
		 */
		@Override
		public int executeUpdate(String sql) throws SQLException {
			return ps.executeUpdate(sql);
		}

		/**
		 * @param sql
		 * @param autoGeneratedKeys
		 * @throws SQLException
		 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
		 */
		@Override
		public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
			return ps.executeUpdate(sql, autoGeneratedKeys);
		}

		/**
		 * @param sql
		 * @param columnIndexes
		 * @throws SQLException
		 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
		 */
		@Override
		public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
			return ps.executeUpdate(sql, columnIndexes);
		}

		/**
		 * @param sql
		 * @param columnNames
		 * @throws SQLException
		 * @see java.sql.Statement#executeUpdate(java.lang.String,
		 *      java.lang.String[])
		 */
		@Override
		public int executeUpdate(String sql, String[] columnNames) throws SQLException {
			return ps.executeUpdate(sql, columnNames);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getConnection()
		 */
		@Override
		public Connection getConnection() throws SQLException {
			return ps.getConnection();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getFetchDirection()
		 */
		@Override
		public int getFetchDirection() throws SQLException {
			return ps.getFetchDirection();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getFetchSize()
		 */
		@Override
		public int getFetchSize() throws SQLException {
			return ps.getFetchSize();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getGeneratedKeys()
		 */
		@Override
		public ResultSet getGeneratedKeys() throws SQLException {
			return ps.getGeneratedKeys();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getMaxFieldSize()
		 */
		@Override
		public int getMaxFieldSize() throws SQLException {
			return ps.getMaxFieldSize();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getMaxRows()
		 */
		@Override
		public int getMaxRows() throws SQLException {
			return ps.getMaxRows();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#getMetaData()
		 */
		@Override
		public ResultSetMetaData getMetaData() throws SQLException {
			return ps.getMetaData();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getMoreResults()
		 */
		@Override
		public boolean getMoreResults() throws SQLException {
			return ps.getMoreResults();
		}

		/**
		 * @param current
		 * @throws SQLException
		 * @see java.sql.Statement#getMoreResults(int)
		 */
		@Override
		public boolean getMoreResults(int current) throws SQLException {
			return ps.getMoreResults(current);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#getParameterMetaData()
		 */
		@Override
		public ParameterMetaData getParameterMetaData() throws SQLException {
			return ps.getParameterMetaData();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getQueryTimeout()
		 */
		@Override
		public int getQueryTimeout() throws SQLException {
			return ps.getQueryTimeout();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getResultSet()
		 */
		@Override
		public ResultSet getResultSet() throws SQLException {
			return ps.getResultSet();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getResultSetConcurrency()
		 */
		@Override
		public int getResultSetConcurrency() throws SQLException {
			return ps.getResultSetConcurrency();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getResultSetHoldability()
		 */
		@Override
		public int getResultSetHoldability() throws SQLException {
			return ps.getResultSetHoldability();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getResultSetType()
		 */
		@Override
		public int getResultSetType() throws SQLException {
			return ps.getResultSetType();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getUpdateCount()
		 */
		@Override
		public int getUpdateCount() throws SQLException {
			return ps.getUpdateCount();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#getWarnings()
		 */
		@Override
		public SQLWarning getWarnings() throws SQLException {
			return ps.getWarnings();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#isClosed()
		 */
		@Override
		public boolean isClosed() throws SQLException {
			return ps.isClosed();
		}

		@Override
		public boolean isCloseOnCompletion() throws SQLException {
			return ps.isCloseOnCompletion();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.Statement#isPoolable()
		 */
		@Override
		public boolean isPoolable() throws SQLException {
			return ps.isPoolable();
		}

		/**
		 * @param iface
		 * @throws SQLException
		 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
		 */
		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return ps.isWrapperFor(iface);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
		 */
		@Override
		public void setArray(int parameterIndex, Array x) throws SQLException {
			ps.setArray(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setAsciiStream(int,
		 *      java.io.InputStream)
		 */
		@Override
		public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
			ps.setAsciiStream(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setAsciiStream(int,
		 *      java.io.InputStream, int)
		 */
		@Override
		public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
			ps.setAsciiStream(parameterIndex, x, length);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setAsciiStream(int,
		 *      java.io.InputStream, long)
		 */
		@Override
		public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
			ps.setAsciiStream(parameterIndex, x, length);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBigDecimal(int,
		 *      java.math.BigDecimal)
		 */
		@Override
		public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
			ps.setBigDecimal(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBinaryStream(int,
		 *      java.io.InputStream)
		 */
		@Override
		public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
			ps.setBinaryStream(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBinaryStream(int,
		 *      java.io.InputStream, int)
		 */
		@Override
		public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
			ps.setBinaryStream(parameterIndex, x, length);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBinaryStream(int,
		 *      java.io.InputStream, long)
		 */
		@Override
		public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
			ps.setBinaryStream(parameterIndex, x, length);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
		 */
		@Override
		public void setBlob(int parameterIndex, Blob x) throws SQLException {
			ps.setBlob(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param inputStream
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
		 */
		@Override
		public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
			ps.setBlob(parameterIndex, inputStream);
		}

		/**
		 * @param parameterIndex
		 * @param inputStream
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream,
		 *      long)
		 */
		@Override
		public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
			ps.setBlob(parameterIndex, inputStream, length);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
		 */
		@Override
		public void setBoolean(int parameterIndex, boolean x) throws SQLException {
			ps.setBoolean(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setByte(int, byte)
		 */
		@Override
		public void setByte(int parameterIndex, byte x) throws SQLException {
			ps.setByte(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setBytes(int, byte[])
		 */
		@Override
		public void setBytes(int parameterIndex, byte[] x) throws SQLException {
			ps.setBytes(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setCharacterStream(int,
		 *      java.io.Reader)
		 */
		@Override
		public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
			ps.setCharacterStream(parameterIndex, reader);
		}

		/**
		 * @param parameterIndex
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setCharacterStream(int,
		 *      java.io.Reader, int)
		 */
		@Override
		public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
			ps.setCharacterStream(parameterIndex, reader, length);
		}

		/**
		 * @param parameterIndex
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setCharacterStream(int,
		 *      java.io.Reader, long)
		 */
		@Override
		public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
			ps.setCharacterStream(parameterIndex, reader, length);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
		 */
		@Override
		public void setClob(int parameterIndex, Clob x) throws SQLException {
			ps.setClob(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
		 */
		@Override
		public void setClob(int parameterIndex, Reader reader) throws SQLException {
			ps.setClob(parameterIndex, reader);
		}

		/**
		 * @param parameterIndex
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
		 */
		@Override
		public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
			ps.setClob(parameterIndex, reader, length);
		}

		/**
		 * @param name
		 * @throws SQLException
		 * @see java.sql.Statement#setCursorName(java.lang.String)
		 */
		@Override
		public void setCursorName(String name) throws SQLException {
			ps.setCursorName(name);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
		 */
		@Override
		public void setDate(int parameterIndex, Date x) throws SQLException {
			ps.setDate(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date,
		 *      java.util.Calendar)
		 */
		@Override
		public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
			ps.setDate(parameterIndex, x, cal);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setDouble(int, double)
		 */
		@Override
		public void setDouble(int parameterIndex, double x) throws SQLException {
			ps.setDouble(parameterIndex, x);
		}

		/**
		 * @param enable
		 * @throws SQLException
		 * @see java.sql.Statement#setEscapeProcessing(boolean)
		 */
		@Override
		public void setEscapeProcessing(boolean enable) throws SQLException {
			ps.setEscapeProcessing(enable);
		}

		/**
		 * @param direction
		 * @throws SQLException
		 * @see java.sql.Statement#setFetchDirection(int)
		 */
		@Override
		public void setFetchDirection(int direction) throws SQLException {
			ps.setFetchDirection(direction);
		}

		/**
		 * @param rows
		 * @throws SQLException
		 * @see java.sql.Statement#setFetchSize(int)
		 */
		@Override
		public void setFetchSize(int rows) throws SQLException {
			ps.setFetchSize(rows);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setFloat(int, float)
		 */
		@Override
		public void setFloat(int parameterIndex, float x) throws SQLException {
			ps.setFloat(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setInt(int, int)
		 */
		@Override
		public void setInt(int parameterIndex, int x) throws SQLException {
			ps.setInt(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setLong(int, long)
		 */
		@Override
		public void setLong(int parameterIndex, long x) throws SQLException {
			ps.setLong(parameterIndex, x);
		}

		/**
		 * @param max
		 * @throws SQLException
		 * @see java.sql.Statement#setMaxFieldSize(int)
		 */
		@Override
		public void setMaxFieldSize(int max) throws SQLException {
			ps.setMaxFieldSize(max);
		}

		/**
		 * @param max
		 * @throws SQLException
		 * @see java.sql.Statement#setMaxRows(int)
		 */
		@Override
		public void setMaxRows(int max) throws SQLException {
			ps.setMaxRows(max);
		}

		/**
		 * @param parameterIndex
		 * @param value
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setNCharacterStream(int,
		 *      java.io.Reader)
		 */
		@Override
		public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
			ps.setNCharacterStream(parameterIndex, value);
		}

		/**
		 * @param parameterIndex
		 * @param value
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setNCharacterStream(int,
		 *      java.io.Reader, long)
		 */
		@Override
		public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
			ps.setNCharacterStream(parameterIndex, value, length);
		}

		/**
		 * @param parameterIndex
		 * @param value
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
		 */
		@Override
		public void setNClob(int parameterIndex, NClob value) throws SQLException {
			ps.setNClob(parameterIndex, value);
		}

		/**
		 * @param parameterIndex
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
		 */
		@Override
		public void setNClob(int parameterIndex, Reader reader) throws SQLException {
			ps.setNClob(parameterIndex, reader);
		}

		/**
		 * @param parameterIndex
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
		 */
		@Override
		public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
			ps.setNClob(parameterIndex, reader, length);
		}

		/**
		 * @param parameterIndex
		 * @param value
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
		 */
		@Override
		public void setNString(int parameterIndex, String value) throws SQLException {
			ps.setNString(parameterIndex, value);
		}

		/**
		 * @param parameterIndex
		 * @param sqlType
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setNull(int, int)
		 */
		@Override
		public void setNull(int parameterIndex, int sqlType) throws SQLException {
			ps.setNull(parameterIndex, sqlType);
		}

		/**
		 * @param parameterIndex
		 * @param sqlType
		 * @param typeName
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
		 */
		@Override
		public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
			ps.setNull(parameterIndex, sqlType, typeName);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
		 */
		@Override
		public void setObject(int parameterIndex, Object x) throws SQLException {
			ps.setObject(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param targetSqlType
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
		 */
		@Override
		public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
			ps.setObject(parameterIndex, x, targetSqlType);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param targetSqlType
		 * @param scaleOrLength
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int,
		 *      int)
		 */
		@Override
		public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
			ps.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
		}

		/**
		 * @param poolable
		 * @throws SQLException
		 * @see java.sql.Statement#setPoolable(boolean)
		 */
		@Override
		public void setPoolable(boolean poolable) throws SQLException {
			ps.setPoolable(poolable);
		}

		/**
		 * @param seconds
		 * @throws SQLException
		 * @see java.sql.Statement#setQueryTimeout(int)
		 */
		@Override
		public void setQueryTimeout(int seconds) throws SQLException {
			ps.setQueryTimeout(seconds);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
		 */
		@Override
		public void setRef(int parameterIndex, Ref x) throws SQLException {
			ps.setRef(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
		 */
		@Override
		public void setRowId(int parameterIndex, RowId x) throws SQLException {
			ps.setRowId(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setShort(int, short)
		 */
		@Override
		public void setShort(int parameterIndex, short x) throws SQLException {
			ps.setShort(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param xmlObject
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
		 */
		@Override
		public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
			ps.setSQLXML(parameterIndex, xmlObject);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
		 */
		@Override
		public void setString(int parameterIndex, String x) throws SQLException {
			ps.setString(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
		 */
		@Override
		public void setTime(int parameterIndex, Time x) throws SQLException {
			ps.setTime(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time,
		 *      java.util.Calendar)
		 */
		@Override
		public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
			ps.setTime(parameterIndex, x, cal);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
		 */
		@Override
		public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
			ps.setTimestamp(parameterIndex, x);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp,
		 *      java.util.Calendar)
		 */
		@Override
		public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
			ps.setTimestamp(parameterIndex, x, cal);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @deprecated
		 * @see java.sql.PreparedStatement#setUnicodeStream(int,
		 *      java.io.InputStream, int)
		 */
		@Override
		@Deprecated
		public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
			ps.setUnicodeStream(parameterIndex, x, length);
		}

		/**
		 * @param parameterIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
		 */
		@Override
		public void setURL(int parameterIndex, URL x) throws SQLException {
			ps.setURL(parameterIndex, x);
		}

		/**
		 * @param <T>
		 * @param iface
		 * @throws SQLException
		 * @see java.sql.Wrapper#unwrap(java.lang.Class)
		 */
		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return ps.unwrap(iface);
		}

	}

	private class SQLConnectionPoolResultSet implements ResultSet {

		private SQLConnectionPoolConnection connection;
		private ResultSet rs;
		private boolean releaseOnCompletion;

		private SQLConnectionPoolResultSet(SQLConnectionPoolConnection connection, ResultSet rs, boolean releaseOnCompletion) {
			this.connection = connection;
			this.rs = rs;
			this.releaseOnCompletion = releaseOnCompletion;
		}

		/**
		 * @param row
		 * @throws SQLException
		 * @see java.sql.ResultSet#absolute(int)
		 */
		@Override
		public boolean absolute(int row) throws SQLException {
			return rs.absolute(row);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#afterLast()
		 */
		@Override
		public void afterLast() throws SQLException {
			rs.afterLast();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#beforeFirst()
		 */
		@Override
		public void beforeFirst() throws SQLException {
			rs.beforeFirst();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#cancelRowUpdates()
		 */
		@Override
		public void cancelRowUpdates() throws SQLException {
			rs.cancelRowUpdates();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#clearWarnings()
		 */
		@Override
		public void clearWarnings() throws SQLException {
			rs.clearWarnings();
		}

		@Override
		public void close() {
			Statement statement = null;
			try {
				statement = getStatement();
			} catch (SQLException e) {
			} finally {
				if (rs != null)
					try {
						rs.close();
					} catch (SQLException e1) {
					} finally {
						rs = null;
					}

				if (statement != null)
					try {
						statement.close();
					} catch (SQLException e1) {
					}

				if (connection != null) {
					if (releaseOnCompletion)
						connection.release();
					connection = null;
				}
			}
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#deleteRow()
		 */
		@Override
		public void deleteRow() throws SQLException {
			rs.deleteRow();
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#findColumn(java.lang.String)
		 */
		@Override
		public int findColumn(String columnLabel) throws SQLException {
			return rs.findColumn(columnLabel);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#first()
		 */
		@Override
		public boolean first() throws SQLException {
			return rs.first();
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getArray(int)
		 */
		@Override
		public Array getArray(int columnIndex) throws SQLException {
			return rs.getArray(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getArray(java.lang.String)
		 */
		@Override
		public Array getArray(String columnLabel) throws SQLException {
			return rs.getArray(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getAsciiStream(int)
		 */
		@Override
		public InputStream getAsciiStream(int columnIndex) throws SQLException {
			return rs.getAsciiStream(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
		 */
		@Override
		public InputStream getAsciiStream(String columnLabel) throws SQLException {
			return rs.getAsciiStream(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBigDecimal(int)
		 */
		@Override
		public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
			return rs.getBigDecimal(columnIndex);
		}

		/**
		 * @param columnIndex
		 * @param scale
		 * @throws SQLException
		 * @deprecated
		 * @see java.sql.ResultSet#getBigDecimal(int, int)
		 */
		@Override
		@Deprecated
		public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
			return rs.getBigDecimal(columnIndex, scale);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
		 */
		@Override
		public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
			return rs.getBigDecimal(columnLabel);
		}

		/**
		 * @param columnLabel
		 * @param scale
		 * @throws SQLException
		 * @deprecated
		 * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
		 */
		@Override
		@Deprecated
		public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
			return rs.getBigDecimal(columnLabel, scale);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBinaryStream(int)
		 */
		@Override
		public InputStream getBinaryStream(int columnIndex) throws SQLException {
			return rs.getBinaryStream(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
		 */
		@Override
		public InputStream getBinaryStream(String columnLabel) throws SQLException {
			return rs.getBinaryStream(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBlob(int)
		 */
		@Override
		public Blob getBlob(int columnIndex) throws SQLException {
			return rs.getBlob(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBlob(java.lang.String)
		 */
		@Override
		public Blob getBlob(String columnLabel) throws SQLException {
			return rs.getBlob(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBoolean(int)
		 */
		@Override
		public boolean getBoolean(int columnIndex) throws SQLException {
			return rs.getBoolean(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBoolean(java.lang.String)
		 */
		@Override
		public boolean getBoolean(String columnLabel) throws SQLException {
			return rs.getBoolean(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getByte(int)
		 */
		@Override
		public byte getByte(int columnIndex) throws SQLException {
			return rs.getByte(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getByte(java.lang.String)
		 */
		@Override
		public byte getByte(String columnLabel) throws SQLException {
			return rs.getByte(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBytes(int)
		 */
		@Override
		public byte[] getBytes(int columnIndex) throws SQLException {
			return rs.getBytes(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getBytes(java.lang.String)
		 */
		@Override
		public byte[] getBytes(String columnLabel) throws SQLException {
			return rs.getBytes(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getCharacterStream(int)
		 */
		@Override
		public Reader getCharacterStream(int columnIndex) throws SQLException {
			return rs.getCharacterStream(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
		 */
		@Override
		public Reader getCharacterStream(String columnLabel) throws SQLException {
			return rs.getCharacterStream(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getClob(int)
		 */
		@Override
		public Clob getClob(int columnIndex) throws SQLException {
			return rs.getClob(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getClob(java.lang.String)
		 */
		@Override
		public Clob getClob(String columnLabel) throws SQLException {
			return rs.getClob(columnLabel);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getConcurrency()
		 */
		@Override
		public int getConcurrency() throws SQLException {
			return rs.getConcurrency();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getCursorName()
		 */
		@Override
		public String getCursorName() throws SQLException {
			return rs.getCursorName();
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getDate(int)
		 */
		@Override
		public Date getDate(int columnIndex) throws SQLException {
			return rs.getDate(columnIndex);
		}

		/**
		 * @param columnIndex
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
		 */
		@Override
		public Date getDate(int columnIndex, Calendar cal) throws SQLException {
			return rs.getDate(columnIndex, cal);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getDate(java.lang.String)
		 */
		@Override
		public Date getDate(String columnLabel) throws SQLException {
			return rs.getDate(columnLabel);
		}

		/**
		 * @param columnLabel
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
		 */
		@Override
		public Date getDate(String columnLabel, Calendar cal) throws SQLException {
			return rs.getDate(columnLabel, cal);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getDouble(int)
		 */
		@Override
		public double getDouble(int columnIndex) throws SQLException {
			return rs.getDouble(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getDouble(java.lang.String)
		 */
		@Override
		public double getDouble(String columnLabel) throws SQLException {
			return rs.getDouble(columnLabel);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getFetchDirection()
		 */
		@Override
		public int getFetchDirection() throws SQLException {
			return rs.getFetchDirection();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getFetchSize()
		 */
		@Override
		public int getFetchSize() throws SQLException {
			return rs.getFetchSize();
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getFloat(int)
		 */
		@Override
		public float getFloat(int columnIndex) throws SQLException {
			return rs.getFloat(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getFloat(java.lang.String)
		 */
		@Override
		public float getFloat(String columnLabel) throws SQLException {
			return rs.getFloat(columnLabel);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getHoldability()
		 */
		@Override
		public int getHoldability() throws SQLException {
			return rs.getHoldability();
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getInt(int)
		 */
		@Override
		public int getInt(int columnIndex) throws SQLException {
			return rs.getInt(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getInt(java.lang.String)
		 */
		@Override
		public int getInt(String columnLabel) throws SQLException {
			return rs.getInt(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getLong(int)
		 */
		@Override
		public long getLong(int columnIndex) throws SQLException {
			return rs.getLong(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getLong(java.lang.String)
		 */
		@Override
		public long getLong(String columnLabel) throws SQLException {
			return rs.getLong(columnLabel);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getMetaData()
		 */
		@Override
		public ResultSetMetaData getMetaData() throws SQLException {
			return rs.getMetaData();
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getNCharacterStream(int)
		 */
		@Override
		public Reader getNCharacterStream(int columnIndex) throws SQLException {
			return rs.getNCharacterStream(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)
		 */
		@Override
		public Reader getNCharacterStream(String columnLabel) throws SQLException {
			return rs.getNCharacterStream(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getNClob(int)
		 */
		@Override
		public NClob getNClob(int columnIndex) throws SQLException {
			return rs.getNClob(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getNClob(java.lang.String)
		 */
		@Override
		public NClob getNClob(String columnLabel) throws SQLException {
			return rs.getNClob(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getNString(int)
		 */
		@Override
		public String getNString(int columnIndex) throws SQLException {
			return rs.getNString(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getNString(java.lang.String)
		 */
		@Override
		public String getNString(String columnLabel) throws SQLException {
			return rs.getNString(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getObject(int)
		 */
		@Override
		public Object getObject(int columnIndex) throws SQLException {
			return rs.getObject(columnIndex);
		}

		@Override
		public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
			return rs.getObject(columnIndex, type);
		}

		/**
		 * @param columnIndex
		 * @param map
		 * @throws SQLException
		 * @see java.sql.ResultSet#getObject(int, java.util.Map)
		 */
		@Override
		public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
			return rs.getObject(columnIndex, map);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getObject(java.lang.String)
		 */
		@Override
		public Object getObject(String columnLabel) throws SQLException {
			return rs.getObject(columnLabel);
		}

		@Override
		public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
			return rs.getObject(columnLabel, type);
		}

		/**
		 * @param columnLabel
		 * @param map
		 * @throws SQLException
		 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
		 */
		@Override
		public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
			return rs.getObject(columnLabel, map);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getRef(int)
		 */
		@Override
		public Ref getRef(int columnIndex) throws SQLException {
			return rs.getRef(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getRef(java.lang.String)
		 */
		@Override
		public Ref getRef(String columnLabel) throws SQLException {
			return rs.getRef(columnLabel);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getRow()
		 */
		@Override
		public int getRow() throws SQLException {
			return rs.getRow();
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getRowId(int)
		 */
		@Override
		public RowId getRowId(int columnIndex) throws SQLException {
			return rs.getRowId(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getRowId(java.lang.String)
		 */
		@Override
		public RowId getRowId(String columnLabel) throws SQLException {
			return rs.getRowId(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getShort(int)
		 */
		@Override
		public short getShort(int columnIndex) throws SQLException {
			return rs.getShort(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getShort(java.lang.String)
		 */
		@Override
		public short getShort(String columnLabel) throws SQLException {
			return rs.getShort(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getSQLXML(int)
		 */
		@Override
		public SQLXML getSQLXML(int columnIndex) throws SQLException {
			return rs.getSQLXML(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getSQLXML(java.lang.String)
		 */
		@Override
		public SQLXML getSQLXML(String columnLabel) throws SQLException {
			return rs.getSQLXML(columnLabel);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getStatement()
		 */
		@Override
		public Statement getStatement() throws SQLException {
			return rs.getStatement();
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getString(int)
		 */
		@Override
		public String getString(int columnIndex) throws SQLException {
			return rs.getString(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getString(java.lang.String)
		 */
		@Override
		public String getString(String columnLabel) throws SQLException {
			return rs.getString(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getTime(int)
		 */
		@Override
		public Time getTime(int columnIndex) throws SQLException {
			return rs.getTime(columnIndex);
		}

		/**
		 * @param columnIndex
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
		 */
		@Override
		public Time getTime(int columnIndex, Calendar cal) throws SQLException {
			return rs.getTime(columnIndex, cal);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getTime(java.lang.String)
		 */
		@Override
		public Time getTime(String columnLabel) throws SQLException {
			return rs.getTime(columnLabel);
		}

		/**
		 * @param columnLabel
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
		 */
		@Override
		public Time getTime(String columnLabel, Calendar cal) throws SQLException {
			return rs.getTime(columnLabel, cal);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getTimestamp(int)
		 */
		@Override
		public Timestamp getTimestamp(int columnIndex) throws SQLException {
			return rs.getTimestamp(columnIndex);
		}

		/**
		 * @param columnIndex
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
		 */
		@Override
		public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
			return rs.getTimestamp(columnIndex, cal);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
		 */
		@Override
		public Timestamp getTimestamp(String columnLabel) throws SQLException {
			return rs.getTimestamp(columnLabel);
		}

		/**
		 * @param columnLabel
		 * @param cal
		 * @throws SQLException
		 * @see java.sql.ResultSet#getTimestamp(java.lang.String,
		 *      java.util.Calendar)
		 */
		@Override
		public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
			return rs.getTimestamp(columnLabel, cal);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getType()
		 */
		@Override
		public int getType() throws SQLException {
			return rs.getType();
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @deprecated
		 * @see java.sql.ResultSet#getUnicodeStream(int)
		 */
		@Override
		@Deprecated
		public InputStream getUnicodeStream(int columnIndex) throws SQLException {
			return rs.getUnicodeStream(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @deprecated
		 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
		 */
		@Override
		@Deprecated
		public InputStream getUnicodeStream(String columnLabel) throws SQLException {
			return rs.getUnicodeStream(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#getURL(int)
		 */
		@Override
		public URL getURL(int columnIndex) throws SQLException {
			return rs.getURL(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#getURL(java.lang.String)
		 */
		@Override
		public URL getURL(String columnLabel) throws SQLException {
			return rs.getURL(columnLabel);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#getWarnings()
		 */
		@Override
		public SQLWarning getWarnings() throws SQLException {
			return rs.getWarnings();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#insertRow()
		 */
		@Override
		public void insertRow() throws SQLException {
			rs.insertRow();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#isAfterLast()
		 */
		@Override
		public boolean isAfterLast() throws SQLException {
			return rs.isAfterLast();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#isBeforeFirst()
		 */
		@Override
		public boolean isBeforeFirst() throws SQLException {
			return rs.isBeforeFirst();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#isClosed()
		 */
		@Override
		public boolean isClosed() throws SQLException {
			return rs.isClosed();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#isFirst()
		 */
		@Override
		public boolean isFirst() throws SQLException {
			return rs.isFirst();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#isLast()
		 */
		@Override
		public boolean isLast() throws SQLException {
			return rs.isLast();
		}

		/**
		 * @param iface
		 * @throws SQLException
		 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
		 */
		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return rs.isWrapperFor(iface);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#last()
		 */
		@Override
		public boolean last() throws SQLException {
			return rs.last();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#moveToCurrentRow()
		 */
		@Override
		public void moveToCurrentRow() throws SQLException {
			rs.moveToCurrentRow();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#moveToInsertRow()
		 */
		@Override
		public void moveToInsertRow() throws SQLException {
			rs.moveToInsertRow();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#next()
		 */
		@Override
		public boolean next() throws SQLException {
			return rs.next();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#previous()
		 */
		@Override
		public boolean previous() throws SQLException {
			return rs.previous();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#refreshRow()
		 */
		@Override
		public void refreshRow() throws SQLException {
			rs.refreshRow();
		}

		/**
		 * @param rows
		 * @throws SQLException
		 * @see java.sql.ResultSet#relative(int)
		 */
		@Override
		public boolean relative(int rows) throws SQLException {
			return rs.relative(rows);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#rowDeleted()
		 */
		@Override
		public boolean rowDeleted() throws SQLException {
			return rs.rowDeleted();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#rowInserted()
		 */
		@Override
		public boolean rowInserted() throws SQLException {
			return rs.rowInserted();
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#rowUpdated()
		 */
		@Override
		public boolean rowUpdated() throws SQLException {
			return rs.rowUpdated();
		}

		/**
		 * @param direction
		 * @throws SQLException
		 * @see java.sql.ResultSet#setFetchDirection(int)
		 */
		@Override
		public void setFetchDirection(int direction) throws SQLException {
			rs.setFetchDirection(direction);
		}

		/**
		 * @param rows
		 * @throws SQLException
		 * @see java.sql.ResultSet#setFetchSize(int)
		 */
		@Override
		public void setFetchSize(int rows) throws SQLException {
			rs.setFetchSize(rows);
		}

		/**
		 * @param <T>
		 * @param iface
		 * @throws SQLException
		 * @see java.sql.Wrapper#unwrap(java.lang.Class)
		 */
		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return rs.unwrap(iface);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
		 */
		@Override
		public void updateArray(int columnIndex, Array x) throws SQLException {
			rs.updateArray(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
		 */
		@Override
		public void updateArray(String columnLabel, Array x) throws SQLException {
			rs.updateArray(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream)
		 */
		@Override
		public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
			rs.updateAsciiStream(columnIndex, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream,
		 *      int)
		 */
		@Override
		public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
			rs.updateAsciiStream(columnIndex, x, length);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream,
		 *      long)
		 */
		@Override
		public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
			rs.updateAsciiStream(columnIndex, x, length);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
		 *      java.io.InputStream)
		 */
		@Override
		public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
			rs.updateAsciiStream(columnLabel, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
		 *      java.io.InputStream, int)
		 */
		@Override
		public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
			rs.updateAsciiStream(columnLabel, x, length);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
		 *      java.io.InputStream, long)
		 */
		@Override
		public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
			rs.updateAsciiStream(columnLabel, x, length);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)
		 */
		@Override
		public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
			rs.updateBigDecimal(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBigDecimal(java.lang.String,
		 *      java.math.BigDecimal)
		 */
		@Override
		public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
			rs.updateBigDecimal(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream)
		 */
		@Override
		public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
			rs.updateBinaryStream(columnIndex, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream,
		 *      int)
		 */
		@Override
		public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
			rs.updateBinaryStream(columnIndex, x, length);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream,
		 *      long)
		 */
		@Override
		public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
			rs.updateBinaryStream(columnIndex, x, length);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
		 *      java.io.InputStream)
		 */
		@Override
		public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
			rs.updateBinaryStream(columnLabel, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
		 *      java.io.InputStream, int)
		 */
		@Override
		public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
			rs.updateBinaryStream(columnLabel, x, length);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
		 *      java.io.InputStream, long)
		 */
		@Override
		public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
			rs.updateBinaryStream(columnLabel, x, length);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
		 */
		@Override
		public void updateBlob(int columnIndex, Blob x) throws SQLException {
			rs.updateBlob(columnIndex, x);
		}

		/**
		 * @param columnIndex
		 * @param inputStream
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream)
		 */
		@Override
		public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
			rs.updateBlob(columnIndex, inputStream);
		}

		/**
		 * @param columnIndex
		 * @param inputStream
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream, long)
		 */
		@Override
		public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
			rs.updateBlob(columnIndex, inputStream, length);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
		 */
		@Override
		public void updateBlob(String columnLabel, Blob x) throws SQLException {
			rs.updateBlob(columnLabel, x);
		}

		/**
		 * @param columnLabel
		 * @param inputStream
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBlob(java.lang.String,
		 *      java.io.InputStream)
		 */
		@Override
		public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
			rs.updateBlob(columnLabel, inputStream);
		}

		/**
		 * @param columnLabel
		 * @param inputStream
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBlob(java.lang.String,
		 *      java.io.InputStream, long)
		 */
		@Override
		public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
			rs.updateBlob(columnLabel, inputStream, length);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBoolean(int, boolean)
		 */
		@Override
		public void updateBoolean(int columnIndex, boolean x) throws SQLException {
			rs.updateBoolean(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)
		 */
		@Override
		public void updateBoolean(String columnLabel, boolean x) throws SQLException {
			rs.updateBoolean(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateByte(int, byte)
		 */
		@Override
		public void updateByte(int columnIndex, byte x) throws SQLException {
			rs.updateByte(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateByte(java.lang.String, byte)
		 */
		@Override
		public void updateByte(String columnLabel, byte x) throws SQLException {
			rs.updateByte(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBytes(int, byte[])
		 */
		@Override
		public void updateBytes(int columnIndex, byte[] x) throws SQLException {
			rs.updateBytes(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])
		 */
		@Override
		public void updateBytes(String columnLabel, byte[] x) throws SQLException {
			rs.updateBytes(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader)
		 */
		@Override
		public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
			rs.updateCharacterStream(columnIndex, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader,
		 *      int)
		 */
		@Override
		public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
			rs.updateCharacterStream(columnIndex, x, length);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader,
		 *      long)
		 */
		@Override
		public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
			rs.updateCharacterStream(columnIndex, x, length);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
		 *      java.io.Reader)
		 */
		@Override
		public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
			rs.updateCharacterStream(columnLabel, reader);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
		 *      java.io.Reader, int)
		 */
		@Override
		public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
			rs.updateCharacterStream(columnLabel, reader, length);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
		 *      java.io.Reader, long)
		 */
		@Override
		public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
			rs.updateCharacterStream(columnLabel, reader, length);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
		 */
		@Override
		public void updateClob(int columnIndex, Clob x) throws SQLException {
			rs.updateClob(columnIndex, x);
		}

		/**
		 * @param columnIndex
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateClob(int, java.io.Reader)
		 */
		@Override
		public void updateClob(int columnIndex, Reader reader) throws SQLException {
			rs.updateClob(columnIndex, reader);
		}

		/**
		 * @param columnIndex
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
		 */
		@Override
		public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
			rs.updateClob(columnIndex, reader, length);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
		 */
		@Override
		public void updateClob(String columnLabel, Clob x) throws SQLException {
			rs.updateClob(columnLabel, x);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)
		 */
		@Override
		public void updateClob(String columnLabel, Reader reader) throws SQLException {
			rs.updateClob(columnLabel, reader);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader,
		 *      long)
		 */
		@Override
		public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
			rs.updateClob(columnLabel, reader, length);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
		 */
		@Override
		public void updateDate(int columnIndex, Date x) throws SQLException {
			rs.updateDate(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
		 */
		@Override
		public void updateDate(String columnLabel, Date x) throws SQLException {
			rs.updateDate(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateDouble(int, double)
		 */
		@Override
		public void updateDouble(int columnIndex, double x) throws SQLException {
			rs.updateDouble(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
		 */
		@Override
		public void updateDouble(String columnLabel, double x) throws SQLException {
			rs.updateDouble(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateFloat(int, float)
		 */
		@Override
		public void updateFloat(int columnIndex, float x) throws SQLException {
			rs.updateFloat(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
		 */
		@Override
		public void updateFloat(String columnLabel, float x) throws SQLException {
			rs.updateFloat(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateInt(int, int)
		 */
		@Override
		public void updateInt(int columnIndex, int x) throws SQLException {
			rs.updateInt(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateInt(java.lang.String, int)
		 */
		@Override
		public void updateInt(String columnLabel, int x) throws SQLException {
			rs.updateInt(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateLong(int, long)
		 */
		@Override
		public void updateLong(int columnIndex, long x) throws SQLException {
			rs.updateLong(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateLong(java.lang.String, long)
		 */
		@Override
		public void updateLong(String columnLabel, long x) throws SQLException {
			rs.updateLong(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)
		 */
		@Override
		public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
			rs.updateNCharacterStream(columnIndex, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader,
		 *      long)
		 */
		@Override
		public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
			rs.updateNCharacterStream(columnIndex, x, length);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String,
		 *      java.io.Reader)
		 */
		@Override
		public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
			rs.updateNCharacterStream(columnLabel, reader);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String,
		 *      java.io.Reader, long)
		 */
		@Override
		public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
			rs.updateNCharacterStream(columnLabel, reader, length);
		}

		/**
		 * @param columnIndex
		 * @param clob
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)
		 */
		@Override
		public void updateNClob(int columnIndex, NClob clob) throws SQLException {
			rs.updateNClob(columnIndex, clob);
		}

		/**
		 * @param columnIndex
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)
		 */
		@Override
		public void updateNClob(int columnIndex, Reader reader) throws SQLException {
			rs.updateNClob(columnIndex, reader);
		}

		/**
		 * @param columnIndex
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)
		 */
		@Override
		public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
			rs.updateNClob(columnIndex, reader, length);
		}

		/**
		 * @param columnLabel
		 * @param clob
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.sql.NClob)
		 */
		@Override
		public void updateNClob(String columnLabel, NClob clob) throws SQLException {
			rs.updateNClob(columnLabel, clob);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)
		 */
		@Override
		public void updateNClob(String columnLabel, Reader reader) throws SQLException {
			rs.updateNClob(columnLabel, reader);
		}

		/**
		 * @param columnLabel
		 * @param reader
		 * @param length
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader,
		 *      long)
		 */
		@Override
		public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
			rs.updateNClob(columnLabel, reader, length);
		}

		/**
		 * @param columnIndex
		 * @param string
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNString(int, java.lang.String)
		 */
		@Override
		public void updateNString(int columnIndex, String string) throws SQLException {
			rs.updateNString(columnIndex, string);
		}

		/**
		 * @param columnLabel
		 * @param string
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNString(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public void updateNString(String columnLabel, String string) throws SQLException {
			rs.updateNString(columnLabel, string);
		}

		/**
		 * @param columnIndex
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNull(int)
		 */
		@Override
		public void updateNull(int columnIndex) throws SQLException {
			rs.updateNull(columnIndex);
		}

		/**
		 * @param columnLabel
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateNull(java.lang.String)
		 */
		@Override
		public void updateNull(String columnLabel) throws SQLException {
			rs.updateNull(columnLabel);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
		 */
		@Override
		public void updateObject(int columnIndex, Object x) throws SQLException {
			rs.updateObject(columnIndex, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @param scaleOrLength
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
		 */
		@Override
		public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
			rs.updateObject(columnIndex, x, scaleOrLength);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateObject(java.lang.String,
		 *      java.lang.Object)
		 */
		@Override
		public void updateObject(String columnLabel, Object x) throws SQLException {
			rs.updateObject(columnLabel, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @param scaleOrLength
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateObject(java.lang.String,
		 *      java.lang.Object, int)
		 */
		@Override
		public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
			rs.updateObject(columnLabel, x, scaleOrLength);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
		 */
		@Override
		public void updateRef(int columnIndex, Ref x) throws SQLException {
			rs.updateRef(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
		 */
		@Override
		public void updateRef(String columnLabel, Ref x) throws SQLException {
			rs.updateRef(columnLabel, x);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateRow()
		 */
		@Override
		public void updateRow() throws SQLException {
			rs.updateRow();
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateRowId(int, java.sql.RowId)
		 */
		@Override
		public void updateRowId(int columnIndex, RowId x) throws SQLException {
			rs.updateRowId(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateRowId(java.lang.String, java.sql.RowId)
		 */
		@Override
		public void updateRowId(String columnLabel, RowId x) throws SQLException {
			rs.updateRowId(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateShort(int, short)
		 */
		@Override
		public void updateShort(int columnIndex, short x) throws SQLException {
			rs.updateShort(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateShort(java.lang.String, short)
		 */
		@Override
		public void updateShort(String columnLabel, short x) throws SQLException {
			rs.updateShort(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param xmlObject
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateSQLXML(int, java.sql.SQLXML)
		 */
		@Override
		public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
			rs.updateSQLXML(columnIndex, xmlObject);
		}

		/**
		 * @param columnLabel
		 * @param xmlObject
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateSQLXML(java.lang.String,
		 *      java.sql.SQLXML)
		 */
		@Override
		public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
			rs.updateSQLXML(columnLabel, xmlObject);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateString(int, java.lang.String)
		 */
		@Override
		public void updateString(int columnIndex, String x) throws SQLException {
			rs.updateString(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateString(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public void updateString(String columnLabel, String x) throws SQLException {
			rs.updateString(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateTime(int, java.sql.Time)
		 */
		@Override
		public void updateTime(int columnIndex, Time x) throws SQLException {
			rs.updateTime(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)
		 */
		@Override
		public void updateTime(String columnLabel, Time x) throws SQLException {
			rs.updateTime(columnLabel, x);
		}

		/**
		 * @param columnIndex
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)
		 */
		@Override
		public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
			rs.updateTimestamp(columnIndex, x);
		}

		/**
		 * @param columnLabel
		 * @param x
		 * @throws SQLException
		 * @see java.sql.ResultSet#updateTimestamp(java.lang.String,
		 *      java.sql.Timestamp)
		 */
		@Override
		public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
			rs.updateTimestamp(columnLabel, x);
		}

		/**
		 * @throws SQLException
		 * @see java.sql.ResultSet#wasNull()
		 */
		@Override
		public boolean wasNull() throws SQLException {
			return rs.wasNull();
		}

	}

	public static int DEFAULT_POOL_LENGTH = 13;

	private static final int MAX_RETRIES = 10;
	private static final long RETRY_WAIT_TIME = 1000;

	private static Throwable getRootException(Throwable e) {
		if (e == null)
			return null;

		Throwable cause = e.getCause();
		if (cause == null)
			return e;

		return getRootException(cause);
	}

	private String dbURL;
	private String dbUserName;

	private String dbPassword;
	private boolean destroying;
	private boolean destroyed;
	private SQLConnectionPoolConnection[] connections;
	private Semaphore semaphore;
	private boolean initialized;

	private Object initLock;
	
	public SQLConnectionPool(int poolLength, String dbDriver, String dbDriverClass, String dbHost, int dbPort, String dbName, String dbUserName, String dbPassword) throws ClassNotFoundException {
		this(poolLength, dbDriver, dbDriverClass, dbHost, dbPort, dbName, dbUserName, dbPassword, false);
	}

	public SQLConnectionPool(int poolLength, String dbDriver, String dbDriverClass, String dbHost, int dbPort, String dbName, String dbUserName, String dbPassword, boolean dbUseSSL) throws ClassNotFoundException {
		this(poolLength, dbDriverClass, dbDriver + "://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=" + dbUseSSL, dbUserName, dbPassword);
	}

	public SQLConnectionPool(int poolLength, String dbDriverClass, String dbURL, String dbUserName, String dbPassword) throws ClassNotFoundException {
		Class.forName(dbDriverClass);

		destroying = false;
		destroyed = false;
		initialized = false;
		initLock = new Object();
		synchronized (initLock) {
			this.dbURL = dbURL;
			this.dbUserName = dbUserName;
			this.dbPassword = dbPassword;

			semaphore = new Semaphore(poolLength, true);
			connections = new SQLConnectionPoolConnection[poolLength];
			initialized = true;
			initLock.notifyAll();
		}
	}
	
	public SQLConnectionPool(String dbDriver, String dbDriverClass, String dbHost, int dbPort, String dbName, String dbUserName, String dbPassword) throws ClassNotFoundException {
		this(DEFAULT_POOL_LENGTH, dbDriver, dbDriverClass, dbHost, dbPort, dbName, dbUserName, dbPassword, false);
	}

	public SQLConnectionPool(String dbDriver, String dbDriverClass, String dbHost, int dbPort, String dbName, String dbUserName, String dbPassword, boolean dbUseSSL) throws ClassNotFoundException {
		this(DEFAULT_POOL_LENGTH, dbDriver, dbDriverClass, dbHost, dbPort, dbName, dbUserName, dbPassword, dbUseSSL);
	}

	private Connection createNewConnection(boolean autoCommit) throws SQLException, InterruptedException {
		for (int counter = 0; counter < MAX_RETRIES; counter++) {
			boolean retry = false;
			Connection result = null;
			try {
				result = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
				result.setAutoCommit(autoCommit);
				retry = false;

				return result;
			} catch (SQLNonTransientConnectionException e) {
				retry = true;
			} catch (SQLException e) {
				Throwable root = getRootException(e);
				if (root instanceof IOException)
					retry = true;
				else
					throw e;
			} finally {
				if (retry)
					Thread.sleep(RETRY_WAIT_TIME);
			}
		}

		throw new SQLException("Max retries reached for connect to database.");
	}

	public void destroy() {
		synchronized (this) {
			if (destroying || destroyed)
				return;

			destroying = true;
		}

		try {
			for (int i = 0; i < connections.length; i++) {
				SQLConnectionPoolConnection connnection = connections[i];
				if (connnection != null)
					synchronized (connnection) {
						try {
							if (connnection.isUsing()) {
								connnection.closeInternal();
								connnection.owner = null;
							}
						} finally {
							connections[i] = null;
						}
					}
			}
		} finally {
			synchronized (this) {
				destroying = false;
				destroyed = true;
			}
		}
	}

	public boolean execute(String command) throws SQLException, InterruptedException {
		for (int counter = 0; counter < MAX_RETRIES; counter++) {
			SQLConnectionPoolConnection connection = null;
			boolean retry = false;
			try {
				connection = getConnectionInternal(true, true);

				return connection.execute(command);
			} catch (SQLNonTransientConnectionException e) {
				retry = true;
			} catch (SQLException e) {
				Throwable root = getRootException(e);
				if (root instanceof IOException)
					retry = true;
				else
					throw e;
			} finally {
				if (connection != null)
					connection.release();

				if (retry)
					Thread.sleep(RETRY_WAIT_TIME);
			}
		}

		throw new SQLException("Max retries reached for execute this command: " + command);
	}

	public ResultSet execute2(String command) throws SQLException, InterruptedException {
		for (int counter = 0; counter < MAX_RETRIES; counter++) {
			SQLConnectionPoolConnection connection = null;
			boolean retry = false;
			boolean error = false;
			try {
				connection = getConnectionInternal(true, true);

				return connection.execute2(command);
			} catch (SQLNonTransientConnectionException e) {
				retry = true;
				error = true;
			} catch (SQLException e) {
				error = true;

				Throwable root = getRootException(e);
				if (root instanceof IOException)
					retry = true;
				else
					throw e;
			} finally {
				if (error && connection != null)
					connection.release();

				if (retry)
					Thread.sleep(RETRY_WAIT_TIME);
			}
		}

		throw new SQLException("Max retries reached for execute this command: " + command);
	}

	public ResultSet executeQuery(String command) throws SQLException, InterruptedException {
		for (int counter = 0; counter < MAX_RETRIES; counter++) {
			SQLConnectionPoolConnection connection = null;
			boolean retry = false;
			boolean error = false;
			try {
				connection = getConnectionInternal(true, true);

				return connection.executeQuery(command);
			} catch (SQLNonTransientConnectionException e) {
				retry = true;
				error = true;
			} catch (SQLException e) {
				error = true;

				Throwable root = getRootException(e);
				if (root instanceof IOException)
					retry = true;
				else
					throw e;
			} finally {
				if (error && connection != null)
					connection.release();

				if (retry)
					Thread.sleep(RETRY_WAIT_TIME);
			}
		}

		throw new SQLException("Max retries reached for execute this query: " + command);
	}

	public ResultSet executeQuery(String command, int resultSetType, int resultSetConcurrency) throws SQLException, InterruptedException {
		for (int counter = 0; counter < MAX_RETRIES; counter++) {
			SQLConnectionPoolConnection connection = null;
			boolean retry = false;
			boolean error = false;
			try {
				connection = getConnectionInternal(true, true);

				return connection.executeQuery(command, resultSetType, resultSetConcurrency);
			} catch (SQLNonTransientConnectionException e) {
				retry = true;
				error = true;
			} catch (SQLException e) {
				error = true;

				Throwable root = getRootException(e);
				if (root instanceof IOException)
					retry = true;
				else
					throw e;
			} finally {
				if (error && connection != null)
					connection.release();

				if (retry)
					Thread.sleep(RETRY_WAIT_TIME);
			}
		}

		throw new SQLException("Max retries reached for execute this query: " + command);
	}

	public <T> T executeTransaction(Transaction<T> transaction) throws SQLException, InterruptedException {
		for (int counter = 0; counter < MAX_RETRIES; counter++) {
			SQLConnectionPoolConnection connection = null;
			boolean retry = false;
			boolean error = false;
			try {
				connection = getConnectionInternal(false, false);

				connection.startTransaction();
				T result = transaction.execute(connection);
				connection.commit();

				return result;
			} catch (SQLNonTransientConnectionException e) {
				retry = true;
				error = true;
			} catch (SQLException e) {
				error = true;

				Throwable root = getRootException(e);
				if (root instanceof IOException)
					retry = true;
				else
					throw e;
			} finally {
				if (connection != null) {
					if (error)
						try {
							connection.rollBack();
						} catch (SQLException e) {
						}
					connection.release();
				}
				if (retry)
					Thread.sleep(RETRY_WAIT_TIME);
			}
		}

		throw new SQLException("Max retries reached for execute this transaction fully.");
	}

	public int executeUpdate(String command) throws SQLException, InterruptedException {
		for (int counter = 0; counter < MAX_RETRIES; counter++) {
			SQLConnectionPoolConnection connection = null;
			boolean retry = false;
			try {
				connection = getConnectionInternal(true, true);

				return connection.executeUpdate(command);
			} catch (SQLNonTransientConnectionException e) {
				retry = true;
			} catch (SQLException e) {
				Throwable root = getRootException(e);
				if (root instanceof IOException)
					retry = true;
				else
					throw e;
			} finally {
				if (connection != null)
					connection.release();

				if (retry)
					Thread.sleep(RETRY_WAIT_TIME);
			}
		}

		throw new SQLException("Max retries reached for execute this update: " + command);
	}

	public SQLConnection getConnection() throws InterruptedException, SQLException {
		return getConnection(true, true);
	}

	public SQLConnection getConnection(boolean releaseOnCompletion, boolean autoCommit) throws InterruptedException, SQLException {
		return getConnectionInternal(releaseOnCompletion, autoCommit);
	}

	private SQLConnectionPoolConnection getConnectionInternal(boolean releaseOnCompletion, boolean autoCommit) throws InterruptedException, SQLException {
		if (!semaphore.tryAcquire(10, TimeUnit.SECONDS))
			throw new SQLException("Timeout to get a connection from pool.");

		return getFreeConnection(releaseOnCompletion, autoCommit);
	}

	public String getDBPassword() {
		return dbPassword;
	}

	public String getDBURL() {
		return dbURL;
	}

	public String getDBUsername() {
		return dbUserName;
	}

	private SQLConnectionPoolConnection getFreeConnection(boolean releaseOnCompletion, boolean autoCommit) throws InterruptedException, SQLException {
		synchronized (initLock) {
			while (!initialized)
				initLock.wait();
		}

		for (int i = 0; i < connections.length; i++) {
			SQLConnectionPoolConnection connection = connections[i];
			if (connection == null) {
				connection = new SQLConnectionPoolConnection(createNewConnection(autoCommit));
				connection.acquire(releaseOnCompletion);
				connections[i] = connection;

				return connection;
			}

			synchronized (connection) {
				if (connection.isUsing())
					continue;

				boolean isClosed = true;
				try {
					isClosed = connection.isClosed();
				} catch (SQLException e) {
				}
				
				if (isClosed)
					connection.reOpen(autoCommit);
				else
					setAutoCommit(connection, autoCommit);
				
				connection.acquire(releaseOnCompletion);

				return connection;
			}
		}

		throw new RuntimeException("No free connection found.");
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	private void setAutoCommit(SQLConnectionPoolConnection connection, boolean autoCommit) throws SQLException, InterruptedException {
		for (int counter = 0; counter < MAX_RETRIES; counter++) {
			boolean retry = false;
			try {
				connection.setAutoCommit(autoCommit);
				retry = false;

				return;
			} catch (SQLNonTransientConnectionException e) {
				retry = true;
			} catch (SQLException e) {
				Throwable root = getRootException(e);
				if (root instanceof IOException)
					retry = true;
				else
					throw e;
			} finally {
				if (retry)
					Thread.sleep(RETRY_WAIT_TIME);
				connection.reOpen(autoCommit);
			}
		}

		throw new SQLException("Max retries reached for set auto commit to " + autoCommit);
	}

	@Override
	public void close() {
		destroy();
	}

}
