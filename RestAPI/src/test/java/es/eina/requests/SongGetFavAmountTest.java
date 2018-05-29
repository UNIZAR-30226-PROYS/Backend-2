package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class SongGetFavAmountTest extends TestBase {

    private EntityUser user;
    private EntityUser user2;
    private EntityAlbum album;

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
        user = UserUtils.addUser(s, "test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user2 = UserUtils.addUser(s, "test-user2", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        album = AlbumUtils.createAlbum(s, user, "Random Album", 1900);
        createSongs(10, album);
        user.favSong(getSong(0));
        user.favSong(getSong(2));
        user.favSong(getSong(7));
        user2.favSong(getSong(7));
        user2.favSong(getSong(3));
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        deleteSongs();
        AlbumCache.deleteAlbum(s, album);
        UserCache.deleteUser(s, user);
        UserCache.deleteUser(s, user2);
        closeSession();
    }

    @Test
    public void testErrorsUnknownSong() {
        JSONObject obj = performTest(new SongRequests().getFavs(-1));
        Assert.assertEquals("unknownSong", obj.getString("error"));
        obj = performTest(new SongRequests().getFavs(Long.MAX_VALUE));
        Assert.assertEquals("unknownSong", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new SongRequests().getFavs(getSong(0).getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("favs"));

        obj = performTest(new SongRequests().getFavs(getSong(2).getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("favs"));

        obj = performTest(new SongRequests().getFavs(getSong(3).getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("favs"));

        obj = performTest(new SongRequests().getFavs(getSong(7).getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(2, obj.getInt("favs"));

        for(int i : new int[]{1, 4, 5, 6, 8, 9}){
            obj = performTest(new SongRequests().getFavs(getSong(i).getId()));
            Assert.assertEquals("ok", obj.getString("error"));
            Assert.assertEquals(0, obj.getInt("favs"));
        }
    }

}
