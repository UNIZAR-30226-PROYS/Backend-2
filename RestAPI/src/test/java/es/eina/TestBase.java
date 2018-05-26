package es.eina;

import es.eina.sql.utils.HibernateUtils;
import es.eina.sql.utils.HibernateUtilsTest;
import org.hibernate.SessionFactory;
import org.junit.Assert;

import java.io.InputStream;

public class TestBase {

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
