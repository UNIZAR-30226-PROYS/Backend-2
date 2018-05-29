package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserIsFollowingUsersTest extends TestBase {

    private EntityUser user;
    private EntityUser user2;
    private EntityUser user3;

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
        user.followUser(user2);
        user.followUser(user3);
        user3.followUser(user);
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        UserCache.deleteUser(s, user);
        UserCache.deleteUser(s, user2);
        UserCache.deleteUser(s, user3);
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new UserRequests().follows("", user2.getNick()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().follows(null, user2.getNick()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new UserRequests().follows(user.getNick(), ""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().follows(user.getNick(), null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new UserRequests().follows("invalid-user", user2.getNick()));
        Assert.assertEquals("user1NotExists", obj.getString("error"));

        obj = performTest(new UserRequests().follows(user.getNick(), "invalid-user"));
        Assert.assertEquals("user2NotExists", obj.getString("error"));
    }

    @Test
    public void testErrorsNotSame() {
        JSONObject obj = performTest(new UserRequests().follows(user2.getNick(), user2.getNick()));
        Assert.assertEquals("sameUser", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new UserRequests().follows(user.getNick(), user2.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        obj = performTest(new UserRequests().follows(user.getNick(), user3.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));

        obj = performTest(new UserRequests().follows(user2.getNick(), user.getNick()));
        Assert.assertEquals("notFollows", obj.getString("error"));
        obj = performTest(new UserRequests().follows(user2.getNick(), user3.getNick()));
        Assert.assertEquals("notFollows", obj.getString("error"));

        obj = performTest(new UserRequests().follows(user3.getNick(), user.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        obj = performTest(new UserRequests().follows(user3.getNick(), user2.getNick()));
        Assert.assertEquals("notFollows", obj.getString("error"));
    }
}
