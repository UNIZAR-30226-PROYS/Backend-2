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

public class UserGetTest extends TestBase {

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
        user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
    }

    @After
    public void endTest(){
        Assert.assertTrue(UserCache.deleteUser(user));
    }

    @Test
    public void testErrorsInvalidArgs(){
        JSONObject obj = new UserRequests().getUserData("unknown-user", user.getToken().getToken());
        Assert.assertTrue(obj.getBoolean("error"));
    }

    @Test
    public void testErrorsNoToken(){
        JSONObject obj = new UserRequests().getUserData(user.getNick(), "invalid-token");
        Assert.assertFalse(obj.getBoolean("error"));
        Assert.assertEquals((long)user.getId(), obj.getJSONObject("profile").getLong("id"));
        Assert.assertFalse(obj.getJSONObject("profile").getBoolean("mail_visible"));
    }

    @Test
    public void testErrorsOK(){
        JSONObject obj = new UserRequests().getUserData(user.getNick(), user.getToken().getToken());
        Assert.assertFalse(obj.getBoolean("error"));
        Assert.assertEquals((long)user.getId(), obj.getJSONObject("profile").getLong("id"));
        Assert.assertTrue(obj.getJSONObject("profile").getBoolean("mail_visible"));
        Assert.assertEquals(user.getMail(), obj.getJSONObject("profile").getString("mail"));
    }

}
