package es.eina.sql;

import com.zaxxer.hikari.HikariConfig;
import es.eina.RestApp;
import es.eina.sql.parameters.ISQLParameter;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.logging.Level;

public class MySQLConnection {

	private HikariDataSource con;

	/**
	 * Create a new SQL connection.
	 * @param host : Host of this connection
	 * @param user : Username of the database
	 * @param pass : Password of the database
	 * @param db : Database to connect
	 */
	@Deprecated
	public MySQLConnection(String host, String user, String pass, String db) {
		System.out.println("Attempting to connect to SQL.");

		con = new HikariDataSource();
		con.setMaximumPoolSize(10);
		//con.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		con.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
		con.addDataSourceProperty("serverName", host);
		//con.addDataSourceProperty("port", "3306");
		con.addDataSourceProperty("databaseName", db);
		con.addDataSourceProperty("user", user);
		con.addDataSourceProperty("password", pass);
	}

	/**
	 * Close a Query to stop using Pool resources.
	 * @param set : Query object to close.
	 */
	public static void closeStatement(ResultSet set) {
		try {
			set.getStatement().getConnection().close();
		} catch (SQLException | NullPointerException e1) {
			//e1.printStackTrace();
		}
		try {
			set.getStatement().close();
		} catch (SQLException | NullPointerException e) {
			//e.printStackTrace();
		}

		try {
			set.close();
		} catch (SQLException | NullPointerException e) {
			//e.printStackTrace();
		}
	}

	/**
	 * Closes all the connections
	 */
	public void onDisable() {
		con.close();
	}

	public boolean runAsyncRawUpdate(String query) {
		Connection con = null;
		boolean updated = false;

		PreparedStatement p = null;

		try {
			con = this.con.getConnection();

			p = con.prepareStatement(query);

			p.executeUpdate();
			updated = true;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// IF STH FAILS WE WILL CLOSE EVERYTHING
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (p != null) {
				try {
					p.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return updated;
	}

	public boolean runAsyncUpdate(MySQLQueries.QueryId queryId, ISQLParameter... parameters) {
		Connection con = null;
		boolean updated = false;
		SQLQueryData queryData = MySQLQueries.getQuery(queryId);
		String query = queryData.getQuery();
		int params = queryData.getParams();

		if (parameters.length < params) {
			RestApp.getInstance().getLogger()
					.log(Level.WARNING, "Couldn't execute query.");
			RestApp.getInstance().getLogger()
					.log(Level.WARNING, "Query: " + query);
			RestApp.getInstance().getLogger()
					.log(Level.WARNING, "Parameters expected: " + params);
			RestApp.getInstance().getLogger()
					.log(Level.WARNING, "Parameters got: " + parameters.length);
		}

		PreparedStatement p = null;

		try {
			con = this.con.getConnection();

			p = con.prepareStatement(query);

			if (parameters.length > 0 && params > 0) {
				for (int i = 0; i < params; i++) {
					p = parameters[i].handleParameter(i + 1, p);
				}
			}
			p.executeUpdate();

			updated = true;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// IF STH FAILS WE WILL CLOSE EVERYTHING
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (p != null) {
				try {
					p.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return updated;
	}

	public ResultSet runAsyncQuery(MySQLQueries.QueryId queryId, ISQLParameter... parameters) {
		Connection con;
		ResultSet res = null;
		SQLQueryData queryData = MySQLQueries.getQuery(queryId);
		String query = queryData.getQuery();
		int params = queryData.getParams();

		if (parameters.length < params) {
			RestApp.getInstance().getLogger()
					.log(Level.WARNING, "Couldn't execute query.");
			RestApp.getInstance().getLogger()
					.log(Level.WARNING, "Query: " + query);
			RestApp.getInstance().getLogger()
					.log(Level.WARNING, "Parameters expected: " + params);
			RestApp.getInstance().getLogger()
					.log(Level.WARNING, "Parameters got: " + parameters.length);
		} else {
			PreparedStatement p;

			try {
				con = this.con.getConnection();

				p = con.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

				if (parameters.length > 0 && params > 0) {
					for (int i = 0; i < params; i++) {
						p = parameters[i].handleParameter(i + 1, p);
					}
				}

				res = p.executeQuery();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return res;
	}

	/**
	 * Perform an <b>ASYNC</b> query.
	 * <p>
	 * <b>NOTE:</b> Don't call this method unless the query is so complex that
	 * the API cannot handle it (e.g. columns needs to be built at runtime).
	 * <p>
	 * Use {@link #runAsyncQuery(MySQLQueries.QueryId, ISQLParameter[])} instead.
	 */
	public ResultSet runAsyncRawQuery(String query) {
		Connection con;
		ResultSet res = null;

		PreparedStatement p;

		try {
			con = this.con.getConnection();
			p = con.prepareStatement(query);

			res = p.executeQuery();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	/**
	 * @param where : String = DO NOT INCLUDE 'WHERE', ONLY CONDITIONS.
	 * @return The number of rows of that table, with that where clauses.
	 */
	public int runAsyncNumRows(String table, String where) {
		Connection con = null;
		int rows = 0;
		PreparedStatement p = null;

		String query = "SELECT count(*) FROM " + table;

		if (where != null && !where.isEmpty()) {
			query += " WHERE " + where;
		}

		try {
			con = this.con.getConnection();

			p = con.prepareStatement(query,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

			ResultSet set = p.executeQuery();
			set.first();
			rows = set.getInt(1);
			set.close();

		} catch (SQLException e) {
			RestApp.getInstance().getLogger().severe("SQL: " + query);
			e.printStackTrace();
		} finally {
			// IF STH FAILS WE WILL CLOSE EVERYTHING
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (p != null) {
				try {
					p.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return rows;
	}

	public static boolean hasColumn(ResultSet set, String column){
		try {
			ResultSetMetaData metadata = set.getMetaData();
			int count = metadata.getColumnCount();
			for(int i = 0; i < count; i++){
				if(metadata.getColumnName(i).equals(column)){
					return true;
				}
			}
		} catch (SQLException ignored) {}

		return false;
	}

}
