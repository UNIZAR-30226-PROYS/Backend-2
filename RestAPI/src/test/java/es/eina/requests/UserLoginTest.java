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
        user = UserUtils.addUser("test-user", "a@a.net", PASS, "Username :D", "Random BIO", new Date(0), "ES");
    }

    @After
    public void endTest(){
        Assert.assertTrue(UserCache.deleteUser(user));
    }

    @Test
    public void testErrorsInvalidArgs(){
        JSONObject obj = new UserRequests().login("", PASS);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().login(null, PASS);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new UserRequests().login(user.getNick(), "");
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().login(user.getNick(), null);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

    }

    @Test
    public void testErrorsUnknownUser(){
        JSONObject obj = new UserRequests().login("invalid-user", PASS);
        Assert.assertEquals("userNotExists", obj.getString("error"));
    }

    @Test
    public void testErrorsPassError(){
        JSONObject obj = new UserRequests().login(user.getNick(), PASS + "abc");
        Assert.assertEquals("passError", obj.getString("error"));
    }

    @Test
    public void testOK(){

        JSONObject obj = new UserRequests().login(user.getNick(), PASS);
        Assert.assertEquals("ok", obj.getString("error"));
    }

}
