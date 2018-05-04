package es.eina.utils;

import es.eina.RestApp;
import es.eina.cache.TokenManager;
import es.eina.cache.UserCache;
import es.eina.crypt.Crypter;
import es.eina.sql.MySQLConnection;
import es.eina.sql.MySQLQueries;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityToken;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.parameters.SQLParameterString;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.annotation.Nullable;
import java.sql.SQLException;

public class AlbumUtils {



    /**
     * Add a new album in the database.
     * @param userID : Creator's ID.
     * @param title : Title given to the album.
     * @param year : Publish year of the album.
     * @param image : Path to the album cover image.
     * @param song : First song of the new album.
     * @return Null if the album couldn't be created, the actual album if it could be created.
     */
    public static @Nullable EntityAlbum createAlbum(long userID, String title, int year, String image, EntitySong song) {

        Transaction transaction = null;
        EntityAlbum entityAlbum;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.getTransaction();
            transaction.begin();

            long newID = (Long) session.createQuery("SELECT MAX(AlbumId) as c FROM album").iterate().next();
            newID++;
            entityAlbum = new EntityAlbum(newID, userID, title, year, image, song);
            session.save(entityAlbum);

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            entityAlbum = null;
        }

        return entityAlbum;

    }
    
    /**
     * Deletes an album from the database.
     * @param albumID : ID of album to delete.

     * @return True if delete was correct, False otherwise.
     */
    public static boolean deleteAlbum(long albumID) {
    	boolean OK = true;
        Transaction transaction = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.getTransaction();
            transaction.begin();

            session.createQuery("DELETE FROM album WHERE album_id = "+ albumID);

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            OK = false;
        }
        return OK;
    }

}
