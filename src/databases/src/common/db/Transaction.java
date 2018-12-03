package common.db;

import java.sql.SQLException;

public interface Transaction<T> {

	T execute(SQLConnection connection) throws SQLException;

}
