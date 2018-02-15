package es.eina.cache;

import es.eina.RestApp;
import es.eina.sql.MySQLConnection;
import es.eina.sql.MySQLQueries;
import es.eina.sql.parameters.SQLParameterLong;
import es.eina.sql.parameters.SQLParameterString;
import es.eina.utils.RandomString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenManager {
    private static final RandomString randomTokenGenerator = new RandomString(16);
    private static final long TOKEN_VALID_TIME = 2592000000L;

    private static Map<String, Token> tokens = new HashMap<>();

    /**
     * Removes from cache all values that has been stored first and have expired. <br>
     * A value is considered expired if it was stored at least {@link TokenManager#TOKEN_VALID_TIME} ms before.
     * @param time : Current epoch time in ms.
     */
    public static void cleanUp(long time){
        List<String> remove = new ArrayList<>();
        for (Token t : tokens.values()) {
            if(t.cleanUp(time)){
                t.onRemove();
                remove.add(t.getUser());
            }
        }

        for (String user : remove) {
            tokens.remove(user);
        }
    }

    /**
     * Loads from database the token of a user if it's not loaded before in cache.<br>
     * @param user : User to load its token data.
     * @return Returns a {@link Token} object containing the values loaded for this username.
     */
    private static Token loadToken(String user){
        ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_USER_TOKEN, new SQLParameterString(user));
        String token;
        long time = 0;
        boolean dirty = false;
        try {
            if(set.first()){
                token = set.getString("token");
                time = set.getLong("time");
            }else{
                token = randomTokenGenerator.nextString();
                time = System.currentTimeMillis() + TOKEN_VALID_TIME;
                dirty = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            token = "";
        }finally {
			MySQLConnection.closeStatement(set);
		}

        return new Token(user, token, time, dirty);
    }

    /**
     * Gets a token for a username. If it's not loaded in cache, will be brought back from database.<br>
     * @param user : User to load Token data.
     * @return Returns a {@link Token} object containing the values loaded for this username.
     */
    public static Token getToken(String user){
        Token token = tokens.get(user);
        if(token == null){
            token = loadToken(user);
            tokens.put(user, token);
        }

        return token;
    }

    /**
     * Checks which values are "dirty" and should be backed up in the database. Then, performs this backup.
     */
    public static void checkRemove() {
        System.out.println("Update hashes to MySQL");
        for (Token data : tokens.values()) {
            data.onRemove();
        }
    }

    /**
     * Deletes a Token for a user loaded in cache.
     * @param user : User to delete Token data.
     */
	public static void removeToken(String user) {
		tokens.remove(user);
	}


	public static class Token {

        private static long CACHE_LOAD_TIME = 3600000L;

        private String token;
        private final String user;
        private long token_time;
        private boolean dirty;
        private long loadTime;

        /**
         * Construct Token data
         * @param user : Username for this token
         * @param token : Token string code
         * @param token_time : Epoch time in ms this token will expire
         */
        private Token(String user, String token, long token_time){
            this(user, token, token_time, false);
        }

        /**
         * Construct Token data
         * @param user : Username for this token
         * @param token : Token string code
         * @param token_time : Epoch time in ms this token will expire
         * @param dirty : True if this token is dirty, false otherwise.
         */
        private Token(String user, String token, long token_time, boolean dirty){
            this.token = token;
            this.user = user;
            this.token_time = token_time;
            loadTime = System.currentTimeMillis();
            this.dirty = dirty;
        }

        /**
         * @return Returns the username this token belongs.
         */
        private String getUser(){
            return user;
        }

        /**
         * Updates the token code for this user in cache.
         * @param token : New token code
         */
        public void updateToken(String token){
            this.loadTime = System.currentTimeMillis();
            this.token = token;
            dirty = true;
        }

        /**
         * Check if this {@link Token} is valid for a specific token code.
         * @param token Token code to check.
         * @return True if the token code is equal to this {@link Token} and hasn't expired.
         */
        public boolean isValid(String token){
            return token_time >= System.currentTimeMillis() && this.token.equals(token);
        }

        /**
         * Check if this Token should be removed from cache.
         * @param time : Current Epoch time in ms.
         * @return True if this token has expired and should be removed from cache.
         */
        public boolean cleanUp(long time) {
            return time - loadTime >= CACHE_LOAD_TIME;
        }

        /**
         * Update this Token data to cache if the token code has been updated.
         */
        private void onRemove(){
            if(dirty){
                RestApp.getSql().runAsyncUpdate(MySQLQueries.UPDATE_USER_TOKEN, new SQLParameterString(user), new SQLParameterString(token), new SQLParameterLong(token_time), new SQLParameterString(token), new SQLParameterLong(token_time));
            }
        }

        /**
         * @return The token code of this {@link Token}
         */
        public String getToken() {
            return token;
        }
	}
}
