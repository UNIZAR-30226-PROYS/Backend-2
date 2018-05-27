package es.eina.sql.entities;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.crypt.Crypter;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.UserUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.Transactional;
import java.sql.Date;

public class UserTest extends TestBase {

    public void start(){
        openDB();
    }

    @Test
    public void testCreateUser(){
        start();
        openSession();
        EntityUser user = new EntityUser("random_user", "Usuario 1", "a@a.com",
                Crypter.hashPassword("123456", false), new Date(0),
                "Empty bio", "O1");
        Assert.assertTrue(UserCache.addUser(s, user));

        Assert.assertTrue(UserUtils.userExists(s, "random_user"));
        Assert.assertTrue(!UserUtils.userExists(s, "invalid_user"));
        Assert.assertTrue(UserUtils.checkPassword(user, "123456"));

        Assert.assertTrue(UserCache.deleteUser(s, user));
        Assert.assertTrue(!UserUtils.userExists(s, "random_user"));
        closeSession();
    }

}
