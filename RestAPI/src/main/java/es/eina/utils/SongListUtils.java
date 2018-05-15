package es.eina.utils;

import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.crypt.Crypter;
import es.eina.cache.SongListCache;

import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.annotation.Nullable;
import java.sql.Date;

public class SongListUtils {

    /**
     * Add a new user created song list in the database.
     * @param author : List author
     * @param title : Title of the list
     * @return Null if the list couldn't be added, the actual list if it could be added.
     */
    public static @Nullable EntitySongList addUser(EntityUser author, String title) {
        Transaction transaction = null;
        EntitySongList entitySongList;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.getTransaction();
            transaction.begin();

            entitySongList = new EntitySongList(title, author);
            session.save(entitySongList);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            entitySongList = null;
        }

        return entitySongList;

    }
}
