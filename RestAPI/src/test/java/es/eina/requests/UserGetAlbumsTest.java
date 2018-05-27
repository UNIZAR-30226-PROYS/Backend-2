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
        user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user2 = UserUtils.addUser("test-user2", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        album = AlbumUtils.createAlbum(user, "Random Album", 1900);
        album2 = AlbumUtils.createAlbum(user2, "Random Album", 1900);
        album3 = AlbumUtils.createAlbum(user, "Random Album", 1900);
    }

    @After
    public void endTest() {
        AlbumCache.deleteAlbum(album);
        AlbumCache.deleteAlbum(album2);
        AlbumCache.deleteAlbum(album3);
        UserCache.deleteUser(user);
        UserCache.deleteUser(user2);
    }

    @Test
    public void testInvalidArgs(){
        JSONObject obj = new UserRequests().getUserAlbums("");
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().getUserAlbums(null);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testUnknownAlbum(){
        JSONObject obj = new UserRequests().getUserAlbums("unknown-user");
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testOK(){
        JSONObject obj = new UserRequests().getUserAlbums(user.getNick());
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(2, obj.getInt("size"));

        Assert.assertEquals(1, SQLUtils.getRowCountSQL("albums", "user_id = " + user.getId() + " and id = " + album.getAlbumId()));
        Assert.assertEquals(0, SQLUtils.getRowCountSQL("albums", "user_id = " + user.getId() + " and id = " + album2.getAlbumId()));
        Assert.assertEquals(1, SQLUtils.getRowCountSQL("albums", "user_id = " + user.getId() + " and id = " + album3.getAlbumId()));
    }


}
