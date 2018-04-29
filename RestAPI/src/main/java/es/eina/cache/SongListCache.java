package es.eina.cache;

import es.eina.sql.entities.EntitySongList;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SongListCache {
    private static Map<Long, EntitySongList> lists = new ConcurrentHashMap<>();

    /**
     * Removes from cache all entities that has been stored first and have expired.
     * A value is considered expired if isEntityValid() returns false (it is the entity has benn stored "x" ms before).
     *
     * @param time : Current epoch time in ms.
     */
    public static void cleanUp(long time) {
        List<EntitySongList> remove = new ArrayList<>();

        for (EntitySongList data : lists.values()) {
            if (!data.isEntityValid(time)) {
                remove.add(data);
            }
        }

        saveEntities(remove);
    }

    public static void forceSave() {
        saveEntities(lists.values());
    }

    private static void saveEntities(Collection<EntitySongList> remove) {
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            for (EntitySongList data : remove) {
                if (data.isDirty()) {
                    try {
                        tr = session.beginTransaction();
                        session.saveOrUpdate(data);
                        tr.commit();
                        lists.remove(data.getId());
                    } catch (Exception e) {
                        if (tr != null) {
                            tr.rollback();
                        }
                    }
                }
            }
        }
    }


    public static void addSongList(EntitySongList list) {
        long id = list.getId();
        if (!lists.containsKey(id)) {
            lists.put(id, list);
        }
    }

    private static EntitySongList loadSongList(long listId) {
        EntitySongList song = null;
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            song = session.load(EntitySongList.class, listId);
            tr.commit();
            addSongList(song);
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
        }
        return song;
    }

    public static EntitySongList getSongList(long listId) {
        EntitySongList song = lists.get(listId);
        if (song == null) {
            song = loadSongList(listId);
        }

        return song;
    }

    public static boolean deleteSonListg(EntitySongList list) {
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            long id = list.getId();
            tr = session.beginTransaction();
            session.delete(list);
            tr.commit();
            lists.remove(id);
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
