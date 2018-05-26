package es.eina.cache;

import es.eina.RestApp;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SongCache {

    public static boolean addSong(EntitySong song) {
        return HibernateUtils.addEntityToDB(song);
    }

    public static EntitySong getSong(long songId) {
        return HibernateUtils.getEntity(EntitySong.class, songId);
    }

    public static boolean deleteSong(EntitySong song) {
        song.setAlbum(null);
        return HibernateUtils.deleteFromDB(song);
    }

}
