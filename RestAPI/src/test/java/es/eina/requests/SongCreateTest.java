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

public class SongCreateTest extends TestBase {

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
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        AlbumCache.deleteAlbum(s, AlbumCache.getAlbum(s, album.getAlbumId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user2.getId()));
        closeSession();
    }


    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new SongRequests().create("", user.getToken().getToken(), "Title1", album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new SongRequests().create(null, user.getToken().getToken(), "Title1", album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new SongRequests().create(user.getNick(), "", "Title1", album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new SongRequests().create(user.getNick(), null, "Title1", album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new SongRequests().create(user.getNick(), user.getToken().getToken(), "", album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new SongRequests().create(user.getNick(), user.getToken().getToken(), null, album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new SongRequests().create(user.getNick(), user.getToken().getToken(), new String(new char[256]), album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new SongRequests().create("invalid-user", user.getToken().getToken(), "Title1", album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorInvalidToken() {
        JSONObject obj = performTest(new SongRequests().create(user.getNick(), "invalid-token", "Title1", album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidAlbum() {
        JSONObject obj = performTest(new SongRequests().create(user.getNick(), user.getToken().getToken(), "Title1", Long.MAX_VALUE, "O1", "ROCK"));
        Assert.assertEquals("invalidAlbum", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new SongRequests().create(user.getNick(), user.getToken().getToken(), "Title1", album.getAlbumId(), "O1", "ROCK"));
        Assert.assertEquals("ok", obj.getString("error"));
        openSession();
        Assert.assertEquals(1, SQLUtils.getRowCount(s, "song", "id = " + obj.getJSONObject("song").getLong("id") + " and album_id = " + album.getAlbumId()));
        closeSession();
        obj = performTest(new SongRequests().create(user.getNick(), user.getToken().getToken(), "Title1", -1, "O1", "ROCK"));
        Assert.assertEquals("ok", obj.getString("error"));
        openSession();
        Assert.assertEquals(1, SQLUtils.getRowCount(s, "song", "id = " + obj.getJSONObject("song").getLong("id") + " and album_id = null"));
        closeSession();
    }

}
