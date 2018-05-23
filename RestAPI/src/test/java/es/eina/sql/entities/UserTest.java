package es.eina.sql.entities;

import es.eina.TestBase;
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
        super.openDB();
    }

    @Test
    public void testCreateUser(){
        start();
        EntityUser user = new EntityUser("random_user", "Usuario 1", "a@a.com",
                Crypter.hashPassword("123456", false), new Date(0),
                "Empty bio", "O1");
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session s = factory.getCurrentSession();
        Transaction t = s.beginTransaction();
        try {
            s.saveOrUpdate(user);
            t.commit();
        }catch(Exception e){
            e.printStackTrace();
            t.rollback();
            Assert.assertFalse(true);
        }

        Assert.assertTrue(UserUtils.userExists("random_user"));
        Assert.assertTrue(!UserUtils.userExists("invalid_user"));
        Assert.assertTrue(UserUtils.checkPassword(user, "123456"));

        s = factory.getCurrentSession();
        t = s.beginTransaction();
        try {
            s.delete(user);
            t.commit();
        }catch(Exception e){
            e.printStackTrace();
            t.rollback();
            Assert.assertFalse(true);
        }
    }

}
