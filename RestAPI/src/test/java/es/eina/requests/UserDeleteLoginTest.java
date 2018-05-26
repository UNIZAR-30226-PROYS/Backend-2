package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserDeleteLoginTest extends TestBase {

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
        UserCache.deleteUser(user);
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = new UserRequests().deleteLogin("", user.getToken().getToken());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().deleteLogin(null, user.getToken().getToken());
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new UserRequests().deleteLogin(user.getNick(), "");
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().deleteLogin(user.getNick(), null);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = new UserRequests().deleteLogin("invalid-user", user.getToken().getToken());
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsWrongMail() {
        JSONObject obj = new UserRequests().deleteLogin(user.getNick(), "invalid" + user.getToken().getToken());
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsClosedSession() {
        JSONObject obj = new UserRequests().deleteLogin(user.getNick(), user.getToken().getToken());
        Assert.assertEquals("ok", obj.getString("error"));

        Assert.assertEquals(0, SQLUtils.getRowCount("token", "user_id = " + user.getId()));

        obj = new UserRequests().deleteLogin(user.getNick(), "token");
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testOK() {
        JSONObject obj = new UserRequests().deleteLogin(user.getNick(), user.getToken().getToken());
        Assert.assertEquals("ok", obj.getString("error"));

        Assert.assertEquals(0, SQLUtils.getRowCount("token", "user_id = " + user.getId()));
    }

}
