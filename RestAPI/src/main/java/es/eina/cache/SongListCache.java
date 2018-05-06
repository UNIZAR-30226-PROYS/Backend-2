package es.eina.cache;

import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SongListCache {
    private static Map<Long, EntitySongList> lists = new ConcurrentHashMap<>();
    private static final Map<Long, Set<EntitySongList>> userIDtoList = new ConcurrentHashMap<>();

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

    private static void saveEntity(EntitySongList data){
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            try {
                tr = session.beginTransaction();
                session.saveOrUpdate(data);
                tr.commit();
                SongListCache.addSongList(data);
            } catch (Exception e) {
                if (tr != null) {
                    tr.rollback();
                }
            }
            }
    }

    public static void addSongList(EntitySongList list) {
        long id = list.getId();
        if (!lists.containsKey(id)) {
            lists.put(id, list);
        }
        long authorID = list.getAuthor().getId();
        if (!userIDtoList.containsKey(authorID)){
            Set<EntitySongList> userSet = new HashSet<EntitySongList>();
            userSet.add(list);
            userIDtoList.put(id,userSet);
        }else{
            Set<EntitySongList> userSet = userIDtoList.get(authorID);
            userSet.add(list);
            userIDtoList.put(id,userSet);
        }
    }
    public static void deleteSongList(EntitySongList list){
        long listId = list.getId();
        long authorId = list.getAuthor().getId();
        lists.remove(listId);
        Set<EntitySongList> SongList = userIDtoList.get(authorId);
        if (SongList != null){
            SongList.remove(list);
            if (!SongList.isEmpty()){
                userIDtoList.put(authorId, SongList);
            }else{
                userIDtoList.remove(authorId);
            }
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
    private static Set<EntitySongList> loadSongLists(String nick) {
        EntitySongList song = null;
        Transaction tr = null;
        Set<EntitySongList> setSongLists;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            long ownerID = UserCache.getId(nick);
            Query query = session.createQuery("SELECT p FROM EntitySongList WHERE p.author_id = :owner");
            query.setParameter("owner", ownerID);
            List<EntitySongList> SongList = query.list();
            tr.commit();
             new HashSet<>(SongList);
            for (EntitySongList oneSongList: SongList) {
                addSongList(oneSongList);
            }
            setSongLists = new HashSet<>(SongList);
            return setSongLists;
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
        }
        return null;
    }

    public static EntitySongList getSongList(long listId) {
        EntitySongList song = lists.get(listId);
        if (song == null) {
            song = loadSongList(listId);
        }

        return song;
    }
    public static List<EntitySongList> getSongLists(String nick) {
        long userId = UserCache.getId(nick);
        Set<EntitySongList> userLists = userIDtoList.get(userId);
        if (userLists == null) {
            userLists = loadSongLists(nick);
        }

        return new LinkedList<>(userLists);
    }

    public static boolean deleteSonList(long listId) {
        EntitySongList list = getSongList(listId);
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            session.delete(list);
            tr.commit();
            deleteSongList(list);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        }

        return false;
    }

    public static int addSongs(long listId, List<Long> songsId, long authorId) {
        EntitySongList songList = SongListCache.getSongList(listId);
        if (songList == null) {
            return 1;
        }
        if (songList.getAuthor().getId() == authorId) {
            return 2;
        }
        for (Long id: songsId
             ) {
            //Not Implemented yet
        }

        return 0;
    }
}
