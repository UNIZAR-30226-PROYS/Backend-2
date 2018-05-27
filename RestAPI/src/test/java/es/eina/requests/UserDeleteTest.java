package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserDeleteTest extends TestBase {

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
        openSession();
        user = UserUtils.addUser(s, "test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        UserCache.deleteUser(s, UserCache.getUser(s, user.getNick()));
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = new UserRequests().deleteUserData(user.getNick(), "");
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().deleteUserData(user.getNick(), null);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = new UserRequests().deleteUserData("invalid-user", user.getToken().getToken());
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken() {
        JSONObject obj = new UserRequests().deleteUserData(user.getNick(), "invalid-token");
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testOK() {
        JSONObject obj = new UserRequests().deleteUserData(user.getNick(), user.getToken().getToken());
        Assert.assertEquals("ok", obj.getString("error"));
        openSession();
        Assert.assertEquals(0, SQLUtils.getRowCountSQL(s,"users", "id = " + user.getId()));
        closeSession();
    }

}
