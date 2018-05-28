package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserLoginTest extends TestBase {

    private static final String PASS = "123456";
    private EntityUser user;

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
        closeSession();
    }

    @After
    public void endTest(){
        openSession();
        Assert.assertTrue(UserCache.deleteUser(s, user));
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs(){
        JSONObject obj = performTest(new UserRequests().login("", PASS));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().login(null, PASS));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new UserRequests().login(user.getNick(), ""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().login(user.getNick(), null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

    }

    @Test
    public void testErrorsUnknownUser(){
        JSONObject obj = performTest(new UserRequests().login("invalid-user", PASS));
        Assert.assertEquals("userNotExists", obj.getString("error"));
    }

    @Test
    public void testErrorsPassError(){
        JSONObject obj = performTest(new UserRequests().login(user.getNick(), PASS + "abc"));
        Assert.assertEquals("passError", obj.getString("error"));
    }

    @Test
    public void testOK(){

        JSONObject obj = performTest(new UserRequests().login(user.getNick(), PASS));
        Assert.assertEquals("ok", obj.getString("error"));
    }

}
