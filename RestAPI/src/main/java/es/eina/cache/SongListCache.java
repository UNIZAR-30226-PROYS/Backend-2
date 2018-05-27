package es.eina.cache;

import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.*;

public class SongListCache {

    /**
     * Removes from cache all entities that has been stored first and have expired.
     * A value is considered expired if isEntityValid() returns false (it is the entity has benn stored "x" ms before).
     *
     * @param time : Current epoch time in ms.
     */


    public static boolean addSongList(EntitySongList list) {
        return HibernateUtils.addEntityToDB(list);
    }
    public static boolean deleteSongList(EntitySongList list){
       return HibernateUtils.deleteFromDB(list);
    }

    public static EntitySongList getSongList(long listId) {
        return HibernateUtils.getEntity(EntitySongList.class, listId);
    }



    private static Set<EntitySongList> loadSongLists(String nick) {
        EntitySongList song = null;
        Transaction tr = null;
        Set<EntitySongList> setSongLists;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            long ownerID = UserCache.getId(nick);
            Query query = session.createQuery("FROM song_list S WHERE S.userId = :owner");
            query.setParameter("owner", ownerID);
            List<EntitySongList> SongList = query.list();
            tr.commit();
            new HashSet<>(SongList);
            for (EntitySongList oneSongList: SongList) {
                addSongList(oneSongList);
            }
            setSongLists = new LinkedHashSet<>(SongList);
            return setSongLists;
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
        }
        return null;
    }


    public static Set<EntitySongList> getSongLists(String nick) {
        return loadSongLists(nick);
    }


    public static boolean deleteSongList(long listId) {
        return deleteSongList(getSongList(listId));
    }

    private static boolean addSong(EntitySongList list, List<Long> songsId){
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            for (Long id: songsId){
                EntitySong song = SongCache.getSong(id);
                list.getSongs().add(song);
                song.getLists().add(list);
            }

            session.saveOrUpdate(list);
            session.refresh(list);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            return false;
        }
    }

    public static int addSongs(long listId, List<Long> songsId, long authorId) {
        EntitySongList songList = SongListCache.getSongList(listId);
        if (songList == null) {
            //Song List not found
            return 1;
        }
        if (songList.getUserId() != authorId) {
            //Author not the same
            return 2;
        }
        if(addSong(songList, songsId)){
            return 0;
        }else{
            return 3;
        }
    }

    private static boolean removesong(EntitySongList list, List<Long> songsId){
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            for (Long id: songsId){
                EntitySong song = SongCache.getSong(id);
                list.getSongs().remove(song);
                song.getLists().remove(list);
            }

            session.saveOrUpdate(list);
            session.refresh(list);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            return false;
        }
    }

    public static int removeSongs(long listId, List<Long> songsId, long authorId) {
        EntitySongList songList = SongListCache.getSongList(listId);
        if (songList == null) {
            //Song List not found
            return 1;
        }
        if (songList.getUserId() != authorId) {
            //Author not the same
            return 2;
        }
        Transaction transaction = null;
        if(removesong(songList, songsId)){
            return 0;
        }else{
            return 3;
        }

    }

    public static int addFollower(long listId, EntityUser user) {
        EntitySongList songList = SongListCache.getSongList(listId);
        if (songList == null) {
            //Song List not found
            return 1;
        }
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            songList.getFollowed().add(user);
            user.getFollowing().add(songList);
            tr.commit();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            return 3;
        }
    }



    public static int removeFollower(long listId, EntityUser user) {
        EntitySongList songList = SongListCache.getSongList(listId);
        if (songList == null) {
            //Song List not found
            return 1;
        }
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            songList.getFollowed().remove(user);
            user.getFollowing().remove(songList);
            tr.commit();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            return 3;
        }
    }
}
