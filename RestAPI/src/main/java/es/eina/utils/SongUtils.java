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
     * Add a new nick in the database.
     *
     * @param nick : Username of this nick.
     * @param mail : Email of this nick.
     * @param pass : Crypted password of this nick (see {@link Crypter}
     * @return Null if the user couldn't be added, the actual user if it could be added.
     */
    @Transactional
    public static @Nullable
    EntitySong addSong(EntityAlbum album, String title, String country) {

        EntitySong entitySong = new EntitySong(album, title, country);
        if(album != null) {
            album.addSong(entitySong);
        }

        return SongCache.addSong(entitySong) ? entitySong : null;

    }


    public static JSONArray getLastListenedSongs(@NotNull EntityUser user, int limit){
        limit = Math.max(1, limit);
        JSONArray songs = new JSONArray();
        try(Session s = HibernateUtils.getSession()){
            NativeQuery q = s
                    .createSQLQuery("SELECT song_id FROM user_listened_songs WHERE user_id = :id ORDER BY time DESC LIMIT "+limit+";")
                    .setParameter("id", user.getId());
            for(Object o : q.list()){
                songs.put(o);
            }
        }

        return songs;

    }
}
