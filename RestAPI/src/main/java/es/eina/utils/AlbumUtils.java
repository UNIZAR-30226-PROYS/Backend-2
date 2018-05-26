package es.eina.utils;

import es.eina.cache.AlbumCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
    EntityAlbum createAlbum(EntityUser user, String title, int year) {
        EntityAlbum entityAlbum = new EntityAlbum(user, title, year);

        return AlbumCache.addAlbum(entityAlbum) ? entityAlbum : null;

    }

    /**
     * Deletes an album from the database.
     *
     * @param album : Album to delete.
     * @return True if delete was correct, False otherwise.
     */
    public static boolean deleteAlbum(EntityAlbum album) {
        return AlbumCache.deleteAlbum(album);
    }

}
