package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserFollowUserTest extends TestBase {

    private static final String PASS = "123456";
    private EntityUser user;
    private EntityUser user2;

    @BeforeClass
    public static void start(){
        openDB();
    }

    @AfterClass
    public static void stop(){
        closeDB();
    }

    @Before
    public void setupTest(){
        openSession();
        user = UserUtils.addUser(s,"test-user", "a@a.net", PASS, "Username :D", "Random BIO", new Date(0), "ES");
        user2 = UserUtils.addUser(s,"test-user2", "a@a.net", PASS, "Username :D", "Random BIO", new Date(0), "ES");
        closeSession();
    }

    @After
    public void endTest(){
        openSession();
        UserCache.deleteUser(s, UserCache.getUser(s, user.getNick()));
        UserCache.deleteUser(s, UserCache.getUser(s, user2.getNick()));
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs(){
        JSONObject obj = performTest(new UserRequests().follow("", user2.getNick(), user.getToken().getToken()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().follow(null, user2.getNick(), user.getToken().getToken()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new UserRequests().follow(user.getNick(), "", user.getToken().getToken()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().follow(user.getNick(), null, user.getToken().getToken()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

    }

    @Test
    public void testErrorsUnknownUser1(){
        JSONObject obj = performTest(new UserRequests().follow("invalid-user", user2.getNick(), user.getToken().getToken()));
        Assert.assertEquals("user1NotExists", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser2(){
        JSONObject obj = performTest(new UserRequests().follow(user.getNick(), "invalid-user", user.getToken().getToken()));
        Assert.assertEquals("user2NotExists", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken() {
        JSONObject obj = performTest(new UserRequests().follow(user.getNick(), user2.getNick(), "invalidToken"));
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testOK(){
        JSONObject obj = performTest(new UserRequests().follow(user.getNick(), user2.getNick(), user.getToken().getToken()));
        Assert.assertEquals("ok", obj.getString("error"));
        openSession();
        Assert.assertEquals(1, SQLUtils.getRowCountSQL(s, "user_followers", "follower = " + user.getId() + " and followee = " + user2.getId()));
        closeSession();
    }

    @Test
    public void testAlreadyFollowing(){
        JSONObject obj = performTest(new UserRequests().follow(user.getNick(), user2.getNick(), user.getToken().getToken()));
        Assert.assertEquals("ok", obj.getString("error"));
        openSession();
        Assert.assertEquals(1, SQLUtils.getRowCountSQL(s, "user_followers", "follower = " + user.getId() + " and followee = " + user2.getId()));
        closeSession();

        obj = performTest(new UserRequests().follow(user.getNick(), user2.getNick(), user.getToken().getToken()));
        Assert.assertEquals("alreadyFollowing", obj.getString("error"));
        openSession();
        Assert.assertEquals(1, SQLUtils.getRowCountSQL(s, "user_followers", "follower = " + user.getId() + " and followee = " + user2.getId()));
        closeSession();
    }

}
