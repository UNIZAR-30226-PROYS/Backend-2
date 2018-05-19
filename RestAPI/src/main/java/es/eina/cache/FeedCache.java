package es.eina.cache;

import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class FeedCache {
    private static final String SQL_QUERY_FOLLOW = "SELECT song_list_id as id, follow_time as time\n" +
            "FROM song_list_user_follows\n" +
            "WHERE user_id = :user_id\n" +
            "ORDER BY follow_time DESC\n" +
            "LIMIT :amount";
    private static final String SQL_QUERY_REPR = "SELECT song_id as id, time\n" +
            "FROM song_reproductions\n" +
            "WHERE user_id = :user_id\n" +
            "ORDER BY time DESC\n" +
            "LIMIT :amount";
    private static final String SQL_QUERY_SONG = "SELECT id, upload_time as time\n" +
            "FROM songs\n" +
            "WHERE user_id = :user_id\n" +
            "ORDER BY time DESC\n" +
            "LIMIT :amount";

    private static JSONObject parseResult(Query q){
        JSONObject obj = new JSONObject();
        List data = q.getResultList();
        JSONArray array = new JSONArray();
        for (Object rawCols: data) {
            Object[] cols = (Object[]) rawCols;
            JSONObject song = new JSONObject();
            song.put("id", cols[0]);
            song.put("time", cols[1]);
            array.put(song);
        }

        obj.put("data", array);
        obj.put("results", data.size());
        return obj;
    }

    private static JSONObject getFollowFeed(EntityUser user, Session s, int followAmount){
        Query q = s.createSQLQuery(SQL_QUERY_FOLLOW);
        q.setParameter("user_id", user.getId());
        q.setParameter("amount", followAmount);
        return parseResult(q);
    }

    private static JSONObject getReprFeed(EntityUser user, Session s, int reprAmount){
        Query q = s.createSQLQuery(SQL_QUERY_REPR);
        q.setParameter("user_id", user.getId());
        q.setParameter("amount", reprAmount);
        return parseResult(q);
    }

    private static JSONObject getUploadedSongFeed(EntityUser user, Session s, int songAmount){
        Query q = s.createSQLQuery(SQL_QUERY_SONG);
        q.setParameter("user_id", user.getId());
        q.setParameter("amount", songAmount);
        return parseResult(q);
    }

    public static JSONObject getFeed(EntityUser user, int followAmount, int reprAmount, int songAmount){
        JSONObject object = new JSONObject();
        try(Session s = HibernateUtils.getSessionFactory().openSession()){
            object.put("follow", getFollowFeed(user, s, followAmount));
            object.put("repr", getReprFeed(user, s, reprAmount));
            object.put("songs", getUploadedSongFeed(user, s, songAmount));
        }

        return object;
    }
}
