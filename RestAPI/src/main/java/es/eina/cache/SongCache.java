package es.eina.cache;

import es.eina.sql.entities.EntitySong;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;

public class SongCache {
    public static boolean addSong(Session s, EntitySong song) {
        return HibernateUtils.addEntityToDB(s, song);
    }

    public static EntitySong getSong(Session s, long songId) {
        return HibernateUtils.getEntity(s, EntitySong.class, songId);
    }

    public static boolean deleteSong(Session s, EntitySong song) {
        return HibernateUtils.deleteFromDB(s, song);
    }

}
