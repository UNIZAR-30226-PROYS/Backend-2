package es.eina;

import es.eina.cache.PopularSongCache;
import es.eina.sql.utils.HibernateUtils;
import org.junit.Test;

public class PopularSongTests {

    public void createDBConnection(){
        HibernateUtils.configureDatabase("database.dat");
    }

    public void shutdown(){
        HibernateUtils.shutdown();
    }

    @Test
    public void testPopularSongs(){
        createDBConnection();

        PopularSongCache.getPopularSongs();

        shutdown();


    }
}
