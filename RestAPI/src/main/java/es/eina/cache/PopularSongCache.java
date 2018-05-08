package es.eina.cache;

import es.eina.sql.entities.EntitySong;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;

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

    public static void getPopularSongs(){
        try(Session s = HibernateUtils.getSessionFactory().openSession()){
            Query q = s.createSQLQuery(FULL_QUERY);
            System.out.println(q.list().getClass());
        }
    }
}
