package es.eina.cache;

import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class PopularSongCache {
    private static final String SQL_QUERY_BEFORE = "SELECT s.id, s.user_id, s.title, s.country, s.upload_time,\n" +
            "  CASE WHEN l.likes is NULL THEN 0 ELSE l.likes END AS likes,\n" +
            "  CASE WHEN r.reprs is NULL THEN 0 ELSE r.reprs END AS reprs\n" +
            "\n" +
            "FROM songs s\n" +
            "LEFT JOIN\n" +
            "  (SELECT COUNT(song_id) AS likes, song_id AS like_sid FROM song_likes GROUP BY song_id) l\n" +
            "  ON l.like_sid = s.id\n" +
            "LEFT JOIN\n" +
            "  (SELECT\n" +
            "     COUNT(song_id) AS reprs,\n" +
            "     song_id AS repr_sid FROM song_reproductions GROUP BY song_id) r\n" +
            "    ON r.repr_sid = s.id\n";
    private static final String SQL_QUERY_AFTER = "  ORDER BY likes DESC, reprs DESC\n" +
            "  LIMIT 50;";
    private static final String FULL_QUERY = SQL_QUERY_BEFORE + SQL_QUERY_AFTER;

    private static JSONObject parseResult(Query q){
        JSONObject obj = new JSONObject();
        List data = q.getResultList();
        JSONArray array = new JSONArray();
        for (Object rawCols: data) {
            Object[] cols = (Object[]) rawCols;
            JSONObject song = new JSONObject();
            for (EnumPopularSongValues key: EnumPopularSongValues.values()) {
                song.put(key.getColumn(), cols[key.getId()]);
            }
            array.put(song);
        }

        obj.put("songs", array);
        obj.put("results", data.size());
        return obj;
    }

    public static JSONObject getPopularSongs(){
        try(Session s = HibernateUtils.getSessionFactory().openSession()){
            Query q = s.createSQLQuery(FULL_QUERY);
            //query.setResultTransformer(Transformers.aliasToBean(LogEntry.class))
            return parseResult(q);
        }
    }

    public static JSONObject getPopularSongs(String country){
        if(country == null || country.length() != 2) country = Geolocalizer.DEFAULT_COUNTRY;

        try(Session s = HibernateUtils.getSessionFactory().openSession()){
            Query q = s.createSQLQuery(SQL_QUERY_BEFORE + " WHERE s.country = '"+country+"' " + SQL_QUERY_AFTER);
            //query.setResultTransformer(Transformers.aliasToBean(LogEntry.class))
            return parseResult(q);
        }
    }

    private enum EnumPopularSongValues {
        ID(0, "id"),
        USER_ID(1, "user_id"),
        TITLE(2, "title"),
        COUNTRY(3, "country"),
        UPLOAD_TIME(4, "upload_time"),
        LIKES(5, "likes"),
        REPR(6, "reproductions");

        private int id;
        private String column;

        EnumPopularSongValues(int id, String column){
            this.id = id;
            this.column = column;
        }

        public int getId() {
            return id;
        }

        public String getColumn() {
            return column;
        }
    }
}
