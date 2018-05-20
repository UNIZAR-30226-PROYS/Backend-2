package es.eina.cache;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AlbumCache {

    private static Map<Long, EntityAlbum> albums = new ConcurrentHashMap<>();

    /**
     * Removes from cache all entities that has been stored first and have expired.
     * A value is considered expired if isEntityValid() returns false (it is the entity has benn stored "x" ms before).
     *
     * @param time : Current epoch time in ms.
     */
    public static void cleanUp(long time) {
        List<EntityAlbum> remove = new ArrayList<>();

        for (EntityAlbum data : albums.values()) {
            if (!data.isEntityValid(time)) {
                remove.add(data);
            }
        }

        saveEntities(remove);
    }

    public static void forceSave() {
        saveEntities(albums.values());
    }

    private static void saveEntities(Collection<EntityAlbum> remove) {
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            for (EntityAlbum data : remove) {
                if (data.isDirty()) {
                    try {
                        tr = session.beginTransaction();
                        session.saveOrUpdate(data);
                        tr.commit();
                        albums.remove(data.getAlbumId());
                    } catch (Exception e) {
                        if (tr != null) {
                            tr.rollback();
                        }
                    }
                }
            }
        }
    }

    public static boolean saveEntity(EntityAlbum data){
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            try {
                tr = session.beginTransaction();
                session.saveOrUpdate(data);
                tr.commit();
                AlbumCache.addAlbum(data);
                return true;
            } catch (Exception e) {
                if (tr != null) {
                    tr.rollback();
                    AlbumCache.deleteAlbum(data);
                }
                return false;
            }
        }
    }

    public static void addAlbum(EntityAlbum album) {
        long id = album.getAlbumId();
        if (!albums.containsKey(id)) {
            albums.put(id, album);
        }
    }

    private static EntityAlbum loadAlbum(long albumID) {
        EntityAlbum album = null;
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            album = session.load(EntityAlbum.class, albumID);
            tr.commit();
            addAlbum(album);
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
        }
        return album;
    }

    public static EntityAlbum getAlbum(long albumID) {
        EntityAlbum album = albums.get(albumID);
        if (album == null) {
            album = loadAlbum(albumID);
        }

        return album;
    }

    public static boolean updateAlbum(EntityAlbum album){
        getAlbum(album.getAlbumId());
        if (albums.containsKey(album.getAlbumId())){
            albums.replace(album.getAlbumId(),album);
            return true;
        }
        return false;
    }

    public static boolean deleteAlbum(EntityAlbum album) {
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            long id = album.getAlbumId();
            tr = session.beginTransaction();
            session.delete(album);
            tr.commit();
            albums.remove(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        }

        return false;
    }

}
