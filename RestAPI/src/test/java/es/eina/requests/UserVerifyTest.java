package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserVerifyTest extends TestBase {

    private EntityUser user, verify;

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
        verify = UserUtils.addUser("second-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user.makeAdmin();
    }

    @After
    public void endTest() {
        UserCache.deleteUser(user);
        UserCache.deleteUser(verify);
    }

    @Test
    public void testVerifyInvalidArgs() {
        JSONObject obj = new UserRequests().verifyAccount("", user.getNick(), user.getToken().getToken(), true);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().verifyAccount(null, user.getNick(), user.getToken().getToken(), true);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new UserRequests().verifyAccount(verify.getNick(), "", user.getToken().getToken(), true);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().verifyAccount(verify.getNick(), null, user.getToken().getToken(), true);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new UserRequests().verifyAccount(verify.getNick(), user.getNick(), "", true);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().verifyAccount(verify.getNick(), user.getNick(), null, true);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testUnknownUser() {
        JSONObject obj = new UserRequests().verifyAccount("invalid", user.getNick(), user.getToken().getToken(), true);
        Assert.assertEquals("unknownUser", obj.getString("error"));

        obj = new UserRequests().verifyAccount(verify.getNick(), "invalid", user.getToken().getToken(), true);
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testClosedSession() {
        user.deleteToken();
        JSONObject obj = new UserRequests().verifyAccount(user.getNick(), user.getNick(), "token", true);
        Assert.assertEquals("closedSession", obj.getString("error"));
    }

    @Test
    public void testInvalidToken() {
        JSONObject obj = new UserRequests().verifyAccount(user.getNick(), user.getNick(), "invalid" + user.getToken().getToken(), true);
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testNoPermission() {
        user.demoteAdmin();
        JSONObject obj = new UserRequests().verifyAccount(user.getNick(), user.getNick(), user.getToken().getToken(), true);
        Assert.assertEquals("noPermission", obj.getString("error"));
    }

    @Test
    public void testVerifyOK() {
        JSONObject obj = new UserRequests().verifyAccount(verify.getNick(), user.getNick(), user.getToken().getToken(), true);

        Assert.assertEquals(1, SQLUtils.getRowCountSQL("user_values", "user_id = " + user.getId() + " and verified = '1'"));
        Assert.assertEquals("ok", obj.getString("error"));
    }

    @Test
    public void testUnVerifyOK() {
        verify.verifyAccount();
        JSONObject obj = new UserRequests().verifyAccount(verify.getNick(), user.getNick(), user.getToken().getToken(), false);

        Assert.assertEquals(0, SQLUtils.getRowCountSQL("user_values", "user_id = " + verify.getId() + " and verified = '1'"));
        Assert.assertEquals("ok", obj.getString("error"));
    }

    @Test
    public void testUnVerifyError() {
        verify.verifyAccount();
        JSONObject obj = new UserRequests().verifyAccount(verify.getNick(), user.getNick(), user.getToken().getToken(), false);

        Assert.assertEquals(0, SQLUtils.getRowCountSQL("user_values", "user_id = " + user.getId() + " and verified = '1'"));
        Assert.assertEquals("ok", obj.getString("error"));

        obj = new UserRequests().verifyAccount(verify.getNick(), user.getNick(), user.getToken().getToken(), false);

        Assert.assertEquals(0, SQLUtils.getRowCountSQL("user_values", "user_id = " + user.getId() + " and verified = '1'"));
        Assert.assertEquals("cannotUnverify", obj.getString("error"));
    }

    @Test
    public void testUnVerifyError2() {
        JSONObject obj = new UserRequests().verifyAccount(verify.getNick(), user.getNick(), user.getToken().getToken(), false);

        Assert.assertEquals(0, SQLUtils.getRowCountSQL("user_values", "user_id = " + user.getId() + " and verified = '1'"));
        Assert.assertEquals("cannotUnverify", obj.getString("error"));
    }

}
