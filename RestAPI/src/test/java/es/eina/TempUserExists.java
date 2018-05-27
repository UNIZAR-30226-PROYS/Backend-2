package es.eina;

import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.UserUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.sql.Date;

public class TempUserExists extends TestBase{

    public void createDBConnection(){
        InputStream f = getClass().getResourceAsStream("/database-test.properties");
        HibernateUtils.configureDatabase(f);
    }

    public void shutdown(){
        HibernateUtils.shutdown();
    }

    @Test
    public void testUser(){
        createDBConnection();

        openSession();
        UserUtils.addUser(s, "lAngelP", "a@a.es", "123456", "Username", "Bio:D", new Date(0), "O1");
        closeSession();

        openSession();
        Assert.assertTrue(UserUtils.userExists(s, "lAngelP"));
        Assert.assertFalse(UserUtils.userExists(s, "lAngel"));
        closeSession();

        shutdown();
    }

}
