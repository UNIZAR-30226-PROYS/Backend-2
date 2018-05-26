package es.eina.utils;

import es.eina.cache.SongCache;
import es.eina.crypt.Crypter;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;

import javax.annotation.Nullable;
import javax.transaction.Transactional;

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
    EntitySong addSong(EntityUser author, String title, String country) {

        EntitySong entitySong = new EntitySong(author, title, country);

        return SongCache.addSong(entitySong) ? entitySong : null;

    }
}
