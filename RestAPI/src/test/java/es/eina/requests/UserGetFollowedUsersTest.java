package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserGetFollowedUsersTest extends TestBase {

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
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user2.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user3.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user4.getId()));
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new UserRequests().getFollowed(""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().getFollowed(null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new UserRequests().getFollowed("invalid-user"));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new UserRequests().getFollowed(user.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(2, obj.getInt("size"));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user.getId()));
        Assert.assertTrue(contains(obj.getJSONArray("users"), user2.getId()));
        Assert.assertTrue(contains(obj.getJSONArray("users"), user3.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user4.getId()));

        obj = performTest(new UserRequests().getFollowed(user2.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(0, obj.getInt("size"));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user2.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user3.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user4.getId()));

        obj = performTest(new UserRequests().getFollowed(user3.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("size"));
        Assert.assertTrue(contains(obj.getJSONArray("users"), user.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user2.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user3.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user4.getId()));

        obj = performTest(new UserRequests().getFollowed(user4.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("size"));
        Assert.assertTrue(contains(obj.getJSONArray("users"), user.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user2.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user3.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("users"), user4.getId()));
    }
}
