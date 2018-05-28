package es.eina.cache;

import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NamedQuery;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SongListCache {

    public static boolean addSongList(Session s, EntitySongList list) {
        return HibernateUtils.addEntityToDB(s, list);
    }

    public static EntitySongList getSongList(Session s, long songListID) {
        return HibernateUtils.getEntity(s, EntitySongList.class, songListID);
    }

    public static EntitySongList getSongList(Session s, String author) {
        return HibernateUtils.getEntityByAttribute(s, EntitySongList.class, "author_id", author);
    }

    public static boolean deleteSongList(Session s, EntitySongList songList) {
        return HibernateUtils.deleteFromDB(s, songList);
    }

    public static List<EntitySongList> getSongLists(Session s, String nick) {
        Query<EntitySongList> q = s.createQuery("from song_list where author_id = :author", EntitySongList.class)
                .setParameter("author", nick);
        return q.list();
    }
}
