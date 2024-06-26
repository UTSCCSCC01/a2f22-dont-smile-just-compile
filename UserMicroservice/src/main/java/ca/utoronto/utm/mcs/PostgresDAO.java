package ca.utoronto.utm.mcs;

import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

public class PostgresDAO {
	
	public Connection conn;
    public Statement st;

	public PostgresDAO() {
        Dotenv dotenv = Dotenv.load();
        String addr = dotenv.get("POSTGRES_ADDR");
        String url = "jdbc:postgresql://" + addr + ":5432/root";
		try {
            Class.forName("org.postgresql.Driver");
			this.conn = DriverManager.getConnection(url, "root", "123456");
            this.st = this.conn.createStatement();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// *** implement database operations here *** //

    public ResultSet getUsersFromUid(int uid) throws SQLException {
        String query = "SELECT * FROM users WHERE uid = %d";
        query = String.format(query, uid);
        return this.st.executeQuery(query);
    }

    public ResultSet getUserData(int uid) throws SQLException {
        String query = "SELECT prefer_name as name, email, rides, isdriver FROM users WHERE uid = %d";
        query = String.format(query, uid);
        return this.st.executeQuery(query);
    }

    public void updateUserAttributes(int uid, String email, String password, String prefer_name, Integer rides, Boolean isDriver) throws SQLException {

        String query;
        if (email != null) {
            query = "UPDATE users SET email = '%s' WHERE uid = %d";
            query = String.format(query, email, uid);
            this.st.execute(query);
        }
        if (password != null) {
            query = "UPDATE users SET password = '%s' WHERE uid = %d";
            query = String.format(query, password, uid);
            this.st.execute(query);
        }
        if (prefer_name != null) {
            query = "UPDATE users SET prefer_name = '%s' WHERE uid = %d";
            query = String.format(query, prefer_name, uid);
            this.st.execute(query);
        }
        if ((rides != null)) {
            query = "UPDATE users SET rides = %d WHERE uid = %d";
            query = String.format(query, rides, uid);
            this.st.execute(query);
        }
        if (isDriver != null) {
            query = "UPDATE users SET isdriver = %s WHERE uid = %d";
            query = String.format(query, isDriver, uid);
            this.st.execute(query);
        }
    }

    // added methods

    /**
     *
     * @param name
     * @param email
     * @param password
     * @return true if user successfully registered, false if email already has an account
     * @throws SQLException
     */
    public boolean registerUser(String name, String email, String password) throws SQLException {
        if (this.matchUser(email, null, null, null, null).next()){
            return false;
        }
        String query = "INSERT INTO %s(prefer_name, email, password, rides) VALUES ('%s', '%s', '%s', 0);";
        query = String.format(query, "users", name, email, password);
        this.st.execute(query);
        return true;
    }

    /**
     * Match a user based on 0 or more of the given params as columns to filter by
     * @param email
     * @param password
     * @param prefer_name
     * @param rides
     * @param isDriver
     * @return A set of matching users
     * @throws SQLException
     */
    public ResultSet matchUser(String email, String password, String prefer_name, Integer rides, Boolean isDriver) throws SQLException {
        String query = "SELECT * FROM %s WHERE ";
        query = String.format(query, "users");
        if (email != null) {
            query = query + "email = '%s' AND ";
            query = String.format(query, email);
        }
        if (password != null) {
            query = query + "password = '%s' AND ";
            query = String.format(query, password);
        }
        if (prefer_name != null) {
            query = query + "prefer_name = '%s' AND ";
            query = String.format(query, prefer_name);
        }
        if ((rides != null)) {
            query = query + "rides = %d AND ";
            query = String.format(query, rides);
        }
        if (isDriver != null) {
            query = query + "password = '%b' AND ";
            query = String.format(query, isDriver);
        }

        query = query + "1 = 1;";
        return this.st.executeQuery(query);
    }

    /**
     * Log in a user
     * @param email
     * @param password
     * @return The user matching the login credentials
     * @throws SQLException
     */
    public ResultSet loginUser(String email, String password) throws SQLException {
        String query = "SELECT * FROM %s WHERE email = '%s' AND password = '%s';";
        query = String.format(query, "users", email, password);
        return this.st.executeQuery(query);
    }
}
