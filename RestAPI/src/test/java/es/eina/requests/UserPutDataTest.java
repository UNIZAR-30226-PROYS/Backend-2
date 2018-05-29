package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserPutDataTest extends TestBase {

    private EntityUser user;

    private JSONObject update = buildUpdate(
            new String[] {"username", "mail", "bio", "birth_date"},
            new Object[] {"NewUsername", "random@r.es", "BioDesc", 1L}
    );

    private JSONObject update2 = buildUpdate(
            new String[] {"username", "mail", "bio", "birth_date"},
            new Object[] {1, 2, 3, "abc"}
    );

    private JSONObject passUpdate = buildUpdate(
            new String[] {"pass"},
            new Object[] {new JSONObject("{\"pass0\": \"1234567\", \"pass1\": \"1234567\", \"old_pass\": \"123456\"}")}
    );

    private JSONObject passUpdate2 = buildUpdate(
            new String[] {"pass"},
            new Object[] {1}
    );

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
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        closeSession();
    }

    private JSONObject buildUpdate(String[] keys, Object[] values){
        int m = Math.min(keys.length, values.length);
        JSONObject obj = new JSONObject();
        JSONObject update = new JSONObject();
        for(int i = 0; i < m; i++){
            obj.put(keys[i], values[i]);
        }
        update.put("updates", obj);
        return update;
    }

    @Test
    public void testUpdateUserInvalidArgs() {
        JSONObject obj = performTest(new UserRequests().putUserData(user.getNick(), "", update.toString()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().putUserData(user.getNick(), null, update.toString()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new UserRequests().putUserData(user.getNick(), user.getToken().getToken(), ""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().putUserData(user.getNick(), user.getToken().getToken(), null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testUpdateUserNoUpdate() {
        JSONObject obj = performTest(new UserRequests().putUserData(user.getNick(), user.getToken().getToken(), "{}"));
        Assert.assertEquals("noUpdate", obj.getString("error"));
        obj = performTest(new UserRequests().putUserData(user.getNick(), user.getToken().getToken(), "{\"updates\": 1}"));
        Assert.assertEquals("parseError", obj.getString("error"));
    }

    @Test
    public void testUpdateUserUnknownUser() {
        JSONObject obj = performTest(new UserRequests().putUserData("invalid-user", user.getToken().getToken(), update.toString()));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testUpdateUserClosedSession() {
        openSession();
        user.deleteToken(s);
        closeSession();
        JSONObject obj = performTest(new UserRequests().putUserData(user.getNick(), "invalid-token", update.toString()));
        Assert.assertEquals("closedSession", obj.getString("error"));
    }

    @Test
    public void testUpdateUserInvalidToken() {
        JSONObject obj = performTest(new UserRequests().putUserData(user.getNick(), "invalid-token", update.toString()));
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testUpdateInvalidValue() {
        JSONObject obj = performTest(new UserRequests().putUserData(user.getNick(), user.getToken().getToken(), update2.toString()));

        JSONObject up2 = update2.getJSONObject("updates");
        openSession();
        for(String key : up2.keySet()) {
            if(!"birth_date".equals(key)) {
                Assert.assertEquals(0, SQLUtils.getRowCountSQL(s,"users", "id = " + user.getId() + " and " + key + " = '" + up2.get(key) + "'"));
            }
            Assert.assertEquals("invalidValue", obj.getJSONObject("error").getString(key));
        }
        closeSession();
    }

    @Test
    public void testUpdateOK() {
        JSONObject obj = performTest(new UserRequests().putUserData(user.getNick(), user.getToken().getToken(), update.toString()));
        JSONObject up2 = update.getJSONObject("updates");
        openSession();
        for(String key : up2.keySet()) {
            if(!"birth_date".equals(key)){
                Assert.assertEquals(1, SQLUtils.getRowCountSQL(s, "users", "id = " + user.getId() + " and "+key+" = '"+up2.get(key)+"'"));
            }
            Assert.assertEquals("ok", obj.getJSONObject("error").getString(key));
        }
        closeSession();
    }

    @Test
    public void testUpdatePassInvalidValue() {
        JSONObject obj = performTest(new UserRequests().putUserData(user.getNick(), user.getToken().getToken(), passUpdate2.toString()));

        JSONObject up2 = passUpdate2.getJSONObject("updates");
        for(String key : up2.keySet()) {
            //Assert.assertEquals(0, SQLUtils.getRowCountSQL("users", "id = " + user.getId() + " and "+key+" = '"+up2.get(key)+"'"));
            //Assert.assertEquals(0, SQLUtils.getRowCountSQL("users", "id = " + user.getId() + " and "+key+" = '"+update.get(key)+"'"));
            Assert.assertEquals("invalidValue", obj.getJSONObject("error").getString(key));
        }
    }

    @Test
    public void testUpdatePassOK() {
        JSONObject obj = performTest(new UserRequests().putUserData(user.getNick(), user.getToken().getToken(), passUpdate.toString()));

        JSONObject up2 = passUpdate.getJSONObject("updates");
        for(String key : up2.keySet()) {
            //Assert.assertEquals(1, SQLUtils.getRowCountSQL("users", "id = " + user.getId() + " and "+key+" = '"+up2.get(key)+"'"));
            //Assert.assertEquals(1, SQLUtils.getRowCountSQL("users", "id = " + user.getId() + " and "+key+" = '"+update.get(key)+"'"));
            Assert.assertEquals("ok", obj.getJSONObject("error").getString(key));
        }
    }

}
