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
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserGetFollowersTest extends TestBase {

    private EntityUser user;
    private EntityUser user2;
    private EntityUser user3;
    private EntityUser user4;

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
        user3 = UserUtils.addUser(s, "test-user3", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user4 = UserUtils.addUser(s, "test-user4", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user.followUser(user2);
        user.followUser(user3);
        user3.followUser(user);
        user4.followUser(user);
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        UserCache.deleteUser(s, user);
        UserCache.deleteUser(s, user2);
        UserCache.deleteUser(s, user3);
        UserCache.deleteUser(s, user4);
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new UserRequests().getFollowers(""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().getFollowers(null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new UserRequests().getFollowers("invalid-user"));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new UserRequests().getFollowers(user.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(2, obj.getInt("size"));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user2.getId()));
        Assert.assertTrue(contains(obj.getJSONArray("users"), user3.getId()));
        Assert.assertTrue(contains(obj.getJSONArray("users"), user4.getId()));

        obj = performTest(new UserRequests().getFollowers(user2.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("size"));
        Assert.assertTrue(contains(obj.getJSONArray("users"), user.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user2.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user3.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user4.getId()));

        obj = performTest(new UserRequests().getFollowers(user3.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("size"));
        Assert.assertTrue(contains(obj.getJSONArray("users"), user.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user2.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user3.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user4.getId()));

        obj = performTest(new UserRequests().getFollowers(user4.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(0, obj.getInt("size"));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user2.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user3.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user4.getId()));
    }
}
