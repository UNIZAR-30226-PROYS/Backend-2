package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class SongDeleteTest extends TestBase {

    private EntityUser user;
    private EntityUser user2;
    private EntityAlbum album;
    private EntitySong song;
    private EntitySong song2;

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
        song = SongUtils.addSong(s, album, "Title 1", "O1");
        song2 = SongUtils.addSong(s, null, "Title 1", "O1");
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        SongCache.deleteSong(s, SongCache.getSong(s, song.getId()));
        SongCache.deleteSong(s, SongCache.getSong(s, song2.getId()));
        AlbumCache.deleteAlbum(s, AlbumCache.getAlbum(s, album.getAlbumId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user2.getId()));
        closeSession();
    }


    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new SongRequests().delete("", user.getToken().getToken(), song.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new SongRequests().delete(null, user.getToken().getToken(), song.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new SongRequests().delete(user.getNick(), "", song.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new SongRequests().delete(user.getNick(), null, song.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new SongRequests().delete("invalid-user", user.getToken().getToken(), song.getId()));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorInvalidToken() {
        JSONObject obj = performTest(new SongRequests().delete(user.getNick(), "invalid-token", song.getId()));
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidAlbum() {
        JSONObject obj = performTest(new SongRequests().delete(user.getNick(), user.getToken().getToken(), -1));
        Assert.assertEquals("invalidSong", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownSong() {
        JSONObject obj = performTest(new SongRequests().delete(user.getNick(), user.getToken().getToken(), Long.MAX_VALUE));
        Assert.assertEquals("unknownSong", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new SongRequests().delete(user.getNick(), user.getToken().getToken(), song.getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        openSession();
        Assert.assertEquals(0, SQLUtils.getRowCount(s, "song", "id = " + song.getId()));
        closeSession();

        obj = performTest(new SongRequests().delete(user.getNick(), user.getToken().getToken(), song2.getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        openSession();
        Assert.assertEquals(0, SQLUtils.getRowCount(s, "song", "id = " + song2.getId()));
        closeSession();
    }

}
