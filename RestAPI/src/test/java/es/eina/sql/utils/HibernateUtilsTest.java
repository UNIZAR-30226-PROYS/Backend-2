package es.eina.sql.utils;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class HibernateUtilsTest {

    @Test
    public void sessionLoad() {
        InputStream f = HibernateUtilsTest.class.getResourceAsStream("/database-test.properties");
        //InputStream f = HibernateUtilsTest.class.getResourceAsStream("/database-test.properties");
        Assert.assertNotNull(f);
        SessionFactory session = HibernateUtils.configureDatabase(f);
        Assert.assertNotNull(session);
    }

}
