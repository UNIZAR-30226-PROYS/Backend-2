package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.AlbumUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class AlbumDeleteTest extends TestBase {

    private EntityUser user;
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
        album = AlbumUtils.createAlbum(s, user, "Random Album", 1900);
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        HibernateUtils.deleteFromDB(s, UserCache.getUser(s, user.getNick()));
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new AlbumRequests().delete("", user.getToken().getToken(), album.getAlbumId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new AlbumRequests().delete(null, user.getToken().getToken(), album.getAlbumId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new AlbumRequests().delete(user.getNick(), "", album.getAlbumId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new AlbumRequests().delete(user.getNick(), null, album.getAlbumId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new AlbumRequests().delete("invalid-user", user.getToken().getToken(), album.getAlbumId()));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken() {
        JSONObject obj = performTest(new AlbumRequests().delete(user.getNick(), "invalid+" + user.getToken().getToken(), album.getAlbumId()));
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidAlbum() {
        JSONObject obj = performTest(new AlbumRequests().delete(user.getNick(), user.getToken().getToken(), -1));
        Assert.assertEquals("invalidAlbum", obj.getString("error"));
    }
    @Test
    public void testErrorsUnknownAlbum() {
        JSONObject obj = performTest(new AlbumRequests().delete(user.getNick(), user.getToken().getToken(), Long.MAX_VALUE));
        Assert.assertEquals("unknownAlbum", obj.getString("error"));
    }

    @Test
    public void testOK() {

        JSONObject obj = performTest(new AlbumRequests().delete(user.getNick(), user.getToken().getToken(), album.getAlbumId()));

        openSession();
        Assert.assertEquals(0, SQLUtils.getRowCount(s, "album", "user_id = " + user.getId()));
        closeSession();
        Assert.assertEquals("ok", obj.getString("error"));
    }
}
