package es.eina.utils;


import es.eina.RestApp;
import es.eina.cache.TokenManager;
import es.eina.crypt.Crypter;
import es.eina.sql.MySQLConnection;
import es.eina.sql.MySQLQueries;
import es.eina.sql.parameters.SQLParameterString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserUtils {

	/**
	 * Check if a user and a token is valid.
	 * @param user : User to check
	 * @param token : Token code to check
	 * @return True if the token code is valid for this user, false otherwise.
	 */
	public static boolean validateUserToken(String user, String token){
		return TokenManager.getToken(user).isValid(token);
	}

	/**
	 * Remove from database and cache the token of a user.
	 * @param user : Username to remove token
	 * @return True if all removes went successfully.
	 */
	public static boolean deleteUserToken(String user){
		boolean ok = RestApp.getSql().runAsyncUpdate(MySQLQueries.DELETE_USER_TOKEN, new SQLParameterString(user));
		if(ok){
			TokenManager.removeToken(user);
		}
		return ok;
	}

	/**
	 * Search if a user exists in the database
	 * @param user : Username to search
	 * @return True if the user exists, false otherwise.
	 */
	public static boolean userExists(String user) {
		return RestApp.getSql().runAsyncNumRows("users", "nick = '"+user+"'") > 0;
	}

	/**
	 * Add a new user in the database.
	 * @param user : Username of this user.
	 * @param mail : Email of this user.
	 * @param pass : Crypted password of this user (see {@link Crypter}
	 * @return True if the user has been added, false otherwise.
	 */
	public static boolean addUser(String user, String mail, String pass) {
		return RestApp.getSql().runAsyncUpdate(MySQLQueries.INSERT_USER, new SQLParameterString(user), new SQLParameterString(mail), new SQLParameterString(Crypter.hashPassword(pass, false)));
	}

	/**
	 * Check if a password matches with the password a user used to register.
	 * @param user : Username of the user
	 * @param pass : Password to check.
	 * @return True if the password belongs to this user, false otherwise.
	 */
	public static boolean checkPassword(String user, String pass) {
        ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_USER_DATA_BY_NAME, new SQLParameterString(user));
        String hashedPass = null;

        try {
            set.first();
            hashedPass = set.getString("pass");
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
			MySQLConnection.closeStatement(set);
		}

        return hashedPass != null && Crypter.checkPassword(pass, hashedPass);
	}
}
