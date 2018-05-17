package es.eina.utils;

import es.eina.cache.UserCache;
import es.eina.crypt.Crypter;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.annotation.Nullable;
import java.sql.Date;

public class UserUtils {

	/**
	 * Search if a user exists in the database
	 * @param user : Username to search
	 * @return True if the user exists, false otherwise.
	 */
	public static boolean userExists(String user) {
		return SQLUtils.getRowCount("user", "nick = '"+user+"'") > 0;
	}

    /**
     * Add a new nick in the database.
     * @param nick : Username of this nick.
     * @param mail : Email of this nick.
     * @param pass : Crypted password of this nick (see {@link Crypter}
     * @return Null if the user couldn't be added, the actual user if it could be added.
     */
    public static @Nullable EntityUser addUser(String nick, String mail, String pass, String user, String bio, Date birth, String country) {
        //(nick, username, mail, pass, birth_date, bio, country, register_date)
        Transaction transaction = null;
        EntityUser entityUser;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.getTransaction();
            transaction.begin();

            entityUser = new EntityUser(nick, user, mail,
                    Crypter.hashPassword(pass, false), birth, bio, country);
            session.save(entityUser);
            entityUser.updateToken();
            session.save(entityUser.getToken()); //TODO: DO NOT SAVE HERE

            transaction.commit();

            UserCache.addUser(entityUser);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            entityUser = null;
        }

        return entityUser;

    }

	/**
	 * Check if a password matches with the password a user used to register.
	 * @param user : User to check
	 * @param pass : Password to check.
	 * @return True if the password belongs to this user, false otherwise.
	 */
	public static boolean checkPassword(EntityUser user, String pass) {
        String hashedPass = user != null ? user.getPass() : null;

        return hashedPass != null && Crypter.checkPassword(pass, hashedPass);
	}
}
