package es.eina.utils;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.annotation.Nullable;

public class AlbumUtils {



    /**
     * Add a new album in the database.
     * @param user : Album creator.
     * @param title : Title given to the album.
     * @param year : Publish year of the album.
     * @param image : Path to the album cover image.
     * @return Null if the album couldn't be created, the actual album if it could be created.
     */
    public static @Nullable EntityAlbum createAlbum(EntityUser user, String title, int year, String image) {

        Transaction transaction = null;
        EntityAlbum entityAlbum;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.getTransaction();
            transaction.begin();
            entityAlbum = new EntityAlbum(user, title, year, image);
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
     * @param album : Album to delete.

     * @return True if delete was correct, False otherwise.
     */
    public static boolean deleteAlbum(EntityAlbum album) {
    	boolean OK = true;
        Transaction transaction = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.getTransaction();
            transaction.begin();
            long albumID = album.getAlbumId();
            session.createQuery("DELETE FROM album WHERE id = "+ albumID);

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
