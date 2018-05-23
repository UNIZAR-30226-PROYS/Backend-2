package es.eina;

import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.UserUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class TempUserExists {

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

        Assert.assertTrue(UserUtils.userExists("lAngelP"));
        Assert.assertFalse(UserUtils.userExists("lAngel"));

        shutdown();
    }

}
