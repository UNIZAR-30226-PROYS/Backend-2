package es.eina;

import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.UserUtils;
import org.junit.Assert;
import org.junit.Test;

public class TempUserExists {

    public void createDBConnection(){
        HibernateUtils.configureDatabase("database.dat");
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
