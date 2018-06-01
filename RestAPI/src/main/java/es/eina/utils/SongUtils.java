package es.eina.utils;

import es.eina.cache.SongCache;
import es.eina.crypt.Crypter;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.json.JSONArray;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

public class SongUtils {

    /**
     * Add a new song in the database.
     * @return Null if the user couldn't be added, the actual user if it could be added.
     */
    public static @Nullable
    EntitySong addSong(Session s, EntityAlbum album, String title, String country, String genre) {

        EntitySong entitySong = new EntitySong(album, title, country, genre);
        if(album != null) {
            album.addSong(entitySong);
        }

        return SongCache.addSong(s, entitySong) ? entitySong : null;

    }


    public static @Nullable
    EntitySong addSong(Session s, EntityAlbum album, String title, String country) {
        return addSong(s, album, title, country, "");
    }

    public static JSONArray getLastListenedSongs(Session s, @NotNull EntityUser user, int limit){
        limit = Math.max(1, limit);
        JSONArray songs = new JSONArray();
            NativeQuery q = s
                    .createSQLQuery("SELECT song_id FROM user_listened_songs WHERE user_id = :id ORDER BY time DESC LIMIT "+limit+";")
                    .setParameter("id", user.getId());
            for(Object o : q.list()){
                songs.put(o);
            }

        return songs;

    }
}
