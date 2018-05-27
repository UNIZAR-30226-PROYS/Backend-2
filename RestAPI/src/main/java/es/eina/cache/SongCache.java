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

    private static final Logger LOG = LoggerFactory.getLogger(SongCache.class);

    public static boolean addSong(EntitySong song) {
        return HibernateUtils.addEntityToDB(song);
    }

    public static EntitySong getSong(long songId) {
        return HibernateUtils.getEntity(EntitySong.class, songId);
    }

    public static boolean deleteSong(EntitySong ent) {
        boolean b = false;
        try(Session session = HibernateUtils.getSession()) {
            Transaction tr = session.beginTransaction();
            try {
                EntitySong song = session.get(EntitySong.class, ent.getId());
                session.delete(song);
                tr.commit();
                b = true;
            } catch (Exception e) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                e.printStackTrace();
                LOG.debug("Cannot delete Album from DB", e);
            }
        }

        return b;
    }

}
