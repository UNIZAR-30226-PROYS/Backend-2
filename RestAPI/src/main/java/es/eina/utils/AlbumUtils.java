package es.eina.utils;

import es.eina.cache.AlbumCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import org.hibernate.Session;

import javax.annotation.Nullable;

public class AlbumUtils {


    /**
     * Add a new album in the database.
     *
     * @param user  : Album creator.
     * @param title : Title given to the album.
     * @param year  : Publish year of the album.
     * @return Null if the album couldn't be created, the actual album if it could be created.
     */
    public static @Nullable
    EntityAlbum createAlbum(Session s, EntityUser user, String title, int year) {
        EntityAlbum entityAlbum = new EntityAlbum(user, title, year);
        user.addAlbum(entityAlbum);

        return AlbumCache.addAlbum(s, entityAlbum) ? entityAlbum : null;

    }

}
