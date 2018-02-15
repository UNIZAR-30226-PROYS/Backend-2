package es.eina.cache;

import es.eina.RestApp;
import es.eina.sql.MySQLQueries;
import es.eina.sql.parameters.SQLParameterInteger;
import es.eina.sql.parameters.SQLParameterString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCache {

    private static final long CACHE_INVALIDATE_TIME = 1800000L;

    private static final Map<String, UserData> nameToId = new ConcurrentHashMap<>();
    private static final Map<Integer, UserData> idToName = new ConcurrentHashMap<>();

    /**
     * Removes from cache all values that has been stored first and have expired.
     * A value is considered expired if it was stored at least {@link UserCache#CACHE_INVALIDATE_TIME} ms before.
     * @param time : Current epoch time in ms.
     */
    public static void cleanUp(long time){
        List<UserData> remove = new ArrayList<>();

        for (UserData data: nameToId.values()) {
            if(data.cleanUp(time)){
                remove.add(data);
            }
        }

        for (UserData data: remove) {
            nameToId.remove(data.getUser());
            idToName.remove(data.getId());
        }
    }

    /**
     * Gets a username for an id. If it's not loaded in cache, will be brought back from database.<br>
     * @param id : Id to load username
     * @return Returns the username of the user whose id is provided.
     */
    public static String getUserName(int id){
        UserData data = idToName.get(id);

        if(data == null){
            try {
                data = new UserData(id);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
            idToName.put(id, data);
            nameToId.put(data.getUser(), data);
        }

        return data.getUser();
    }

    /**
     * Gets an id for a username. If it's not loaded in cache, will be brought back from database.<br>
     * @param name : Username to load id
     * @return Returns the id of the user whose username is provided.
     */
    public static int getUserId(String name){
        UserData data = nameToId.get(name);

        if(data == null){
            try {
                data = new UserData(name);
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
            nameToId.put(name, data);
            idToName.put(data.getId(), data);
        }

        return data.getId();
    }

    private static class UserData {
        private String user;
        private int id;
        private long updateTime;

        /**
         * Constructs User data
         * @param id : Id for this User
         * @throws SQLException If there is no user with this id.
         */
        public UserData(int id) throws SQLException {
            this(id, null);
            ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_USER_NAME_FROM_ID, new SQLParameterInteger(id));

            set.first();
            user = set.getString("nick");
        }

        /**
         * Constructs User data
         * @param name : Username for this User
         * @throws SQLException If there is no user with this username.
         */
        public UserData(String name) throws SQLException {
            this(-1, name);
            ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_USER_ID_FROM_NAME, new SQLParameterString(name));

            set.first();
            id = set.getInt("id");
        }

        /**
         * Constructs User data
         * @param id : Id for this User
         * @param user : Username for this user
         */
        public UserData(int id, String user){
            this.id = id;
            this.user = user;
            this.updateTime = System.currentTimeMillis() + CACHE_INVALIDATE_TIME;
        }

        /**
         * Check if this User should be removed from cache.
         * @param time : Current Epoch time in ms.
         * @return True if this user should be removed from cache.
         */
        public boolean cleanUp(long time){
            return updateTime < time;
        }

        /**
         * @return Username of this User.
         */
        public String getUser() {
            return user;
        }

        /**
         * @return Id of this User.
         */
        public int getId() {
            return id;
        }
    }
}
