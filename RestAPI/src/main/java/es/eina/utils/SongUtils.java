package es.eina.utils;

import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.crypt.Crypter;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.sql.Date;

public class SongUtils {

    /**
     * Add a new nick in the database.
     * @param nick : Username of this nick.
     * @param mail : Email of this nick.
     * @param pass : Crypted password of this nick (see {@link Crypter}
     * @return Null if the user couldn't be added, the actual user if it could be added.
     */
    @Transactional
    public static @Nullable
    EntitySong addSong(EntityUser author, String title, String country) {
        Transaction transaction = null;
        EntitySong entitySong;
        try {
            Session session = HibernateUtils.getSessionFactory().getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();

            entitySong = new EntitySong(author, title, country);
            session.save(entitySong);

            transaction.commit();

            SongCache.addSong(entitySong);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            entitySong = null;
            e.printStackTrace();
        }

        return entitySong;

    }
}
