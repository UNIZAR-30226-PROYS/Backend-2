package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class SongGetPopularSongsTest extends TestBase {

    private static final long HALF_DAY = 43200000;
    private static final long HALF_WEEK = 302400000;

    @BeforeClass
    public static void start() {
        openDB();
    }

    @AfterClass
    public static void stop() {
        closeDB();
    }

    @Before
    public void setupTest() {
        openSession();
        createUsers(1);
        createAlbums(1, getUser(0));
        createSongs(10, getAlbum(0));
        for(int i = 0; i < 5; i++){
            getSong(i).setTime(System.currentTimeMillis() - HALF_DAY);
            if(i % 2 == 0){
                getSong(i).setCountry("SP");
            }else{
                getSong(i).setCountry("US");
            }
        }
        for(int i = 5; i < 10; i++){
            getSong(i).setTime(System.currentTimeMillis() - HALF_WEEK);
            if(i % 2 == 0){
                getSong(i).setCountry("SP");
            }else{
                getSong(i).setCountry("US");
            }
        }
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        deleteSongs();
        deleteAlbums();
        deleteUsers();
        closeSession();
    }

    @Test
    public void testGetPopularSongs() {
        JSONObject obj = performTest(new SongRequests().getPopularSongs(1, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 1);
        Assert.assertTrue(
                containsID(obj.getJSONArray("songs"), getSong(0).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(1).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(2).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(3).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(4).getId())
        );

        obj = performTest(new SongRequests().getPopularSongs(10, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 10);
        for(int i = 0; i < 5; i++)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

        obj = performTest(new SongRequests().getPopularSongs(20, false));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 20);
        for(int i = 0; i < 10; i++)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

    }

    @Test
    public void testGetCountryPopularSongs() {
        JSONObject obj = performTest(new SongRequests().getPopularSongs("SP", 1, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 1);
        Assert.assertTrue(
                containsID(obj.getJSONArray("songs"), getSong(0).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(2).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(4).getId())
        );

        obj = performTest(new SongRequests().getPopularSongs("US", 1, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 1);
        Assert.assertTrue(
                        containsID(obj.getJSONArray("songs"), getSong(1).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(3).getId())
        );

        obj = performTest(new SongRequests().getPopularSongs("SP", 10, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 10);
        for(int i = 0; i < 5; i+=2)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

        obj = performTest(new SongRequests().getPopularSongs("US", 10, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 10);
        for(int i = 1; i < 5; i+=2)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

        obj = performTest(new SongRequests().getPopularSongs("SP", 20, false));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 20);
        for(int i = 0; i < 10; i+=2)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

        obj = performTest(new SongRequests().getPopularSongs("US", 20, false));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 20);
        for(int i = 1; i < 10; i+=2)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

    }
}
