package es.eina;

import es.eina.sql.utils.HibernateUtils;
import es.eina.sql.utils.HibernateUtilsTest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Assert;

import java.io.InputStream;

public class TestBase {

    protected Session s;
    protected Transaction t;

    protected void openSession(){
        s = HibernateUtils.getSession();
        t = s.beginTransaction();
    }

    protected void closeSession(){
        t.commit();
        s.close();
    }

    protected static void openDB(){
        InputStream f = HibernateUtilsTest.class.getResourceAsStream("/database-test.properties");
        //InputStream f = HibernateUtilsTest.class.getResourceAsStream("/database-test.properties");
        Assert.assertNotNull(f);
        SessionFactory session = HibernateUtils.configureDatabase(f);
        Assert.assertNotNull(session);
    }

    protected static void closeDB() {
        HibernateUtils.shutdown();
    }
}
