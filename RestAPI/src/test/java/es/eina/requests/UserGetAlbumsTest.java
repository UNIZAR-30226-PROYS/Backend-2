package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.AlbumUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.sql.Date;

public class UserGetAlbumsTest extends TestBase {

    private EntityUser user, user2;
    private EntityAlbum album, album2, album3;

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
        album2 = AlbumUtils.createAlbum(s, user2, "Random Album", 1900);
        album3 = AlbumUtils.createAlbum(s, user, "Random Album", 1900);
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        AlbumCache.deleteAlbum(s, album);
        AlbumCache.deleteAlbum(s, album2);
        AlbumCache.deleteAlbum(s, album3);
        UserCache.deleteUser(s, user);
        UserCache.deleteUser(s, user2);
        closeSession();
    }

    @Test
    public void testInvalidArgs(){
        JSONObject obj = performTest(new UserRequests().getUserAlbums(""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().getUserAlbums(null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testUnknownAlbum(){
        JSONObject obj = performTest(new UserRequests().getUserAlbums("unknown-user"));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testOK(){
        JSONObject obj = performTest(new UserRequests().getUserAlbums(user.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(2, obj.getInt("size"));

        openSession();
        Assert.assertEquals(1, SQLUtils.getRowCountSQL(s, "albums", "user_id = " + user.getId() + " and id = " + album.getAlbumId()));
        Assert.assertEquals(0, SQLUtils.getRowCountSQL(s,"albums", "user_id = " + user.getId() + " and id = " + album2.getAlbumId()));
        Assert.assertEquals(1, SQLUtils.getRowCountSQL(s, "albums", "user_id = " + user.getId() + " and id = " + album3.getAlbumId()));
        closeSession();
    }


}
