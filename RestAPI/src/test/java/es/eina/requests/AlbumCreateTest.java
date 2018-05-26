package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class AlbumCreateTest extends TestBase {

    private EntityUser user;

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
        user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
    }

    @After
    public void endTest() {
        HibernateUtils.deleteFromDB(user);
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = new AlbumRequests().create("", user.getToken().getToken(), "RandomTitle", 2010);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new AlbumRequests().create(null, user.getToken().getToken(), "RandomTitle", 2010);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new AlbumRequests().create(user.getNick(), "", "RandomTitle", 2010);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new AlbumRequests().create(user.getNick(), null, "RandomTitle", 2010);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new AlbumRequests().create(user.getNick(), user.getToken().getToken(), "", 2010);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new AlbumRequests().create(user.getNick(), user.getToken().getToken(), null, 2010);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

    }

    @Test
    public void testErrorsInvalidYear() {
        JSONObject obj = new AlbumRequests().create(user.getNick(), user.getToken().getToken(), "RandomTitle", 1000);
        Assert.assertEquals("invalidYear", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = new AlbumRequests().create("invalid-user", user.getToken().getToken(), "RandomTitle", 2010);
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken() {
        JSONObject obj = new AlbumRequests().create(user.getNick(), "invalid+" + user.getToken().getToken(), "RandomTitle", 2010);
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testOK() {

        JSONObject obj = new AlbumRequests().create(user.getNick(), user.getToken().getToken(), "RandomTitle", 2010);

        Assert.assertEquals(1, SQLUtils.getRowCount("album", "user_id = " + user.getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        EntityAlbum album = AlbumCache.getAlbum(obj.getJSONObject("album").getInt("id"));

        AlbumCache.deleteAlbum(album);
    }
}
