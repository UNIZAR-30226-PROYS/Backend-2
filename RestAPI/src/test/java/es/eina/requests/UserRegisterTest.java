package es.eina.requests;

import es.eina.RestApp;
import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.io.InputStream;
import java.sql.Date;

public class UserRegisterTest extends TestBase {

    private static final String NICK = "test-user";
    private static final String MAIL = "a@a.es";
    private static final String PASS = "123456";
    private static final String USER = "RandomUser";
    private static final long BIRTH = 0;
    private static final String BIO = "Empty bio";


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

    }

    @After
    public void endTest() {

    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = new UserRequests().signup("", MAIL, PASS, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().signup(null, MAIL, PASS, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().signup("123456789012345678901234567890123", MAIL, PASS, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new UserRequests().signup(NICK, "", PASS, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().signup(NICK, null, PASS, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new UserRequests().signup(NICK, MAIL, "", PASS, USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().signup(NICK, MAIL, null, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new UserRequests().signup(NICK, MAIL, PASS, "", USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().signup(NICK, MAIL, PASS, null, USER, BIRTH, BIO);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsExistingUser() {
        openSession();
        EntityUser user = UserUtils.addUser(s, "user1", "a@a.es", "123456", "Usernmae", "bio", new Date(0), "O1");
        closeSession();
        Assert.assertNotNull(user);
        JSONObject obj = new UserRequests().signup(user.getNick(), MAIL, PASS, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("userExists", obj.getString("error"));

        openSession();
        UserCache.deleteUser(s, user);
        closeSession();
    }

    @Test
    public void testErrorsWrongMail() {
        JSONObject obj = new UserRequests().signup(NICK, "invalidMail", PASS, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("wrongMail", obj.getString("error"));
    }

    @Test
    public void testErrorsNotEqualPass() {
        JSONObject obj = new UserRequests().signup(NICK, MAIL, PASS, "a" + PASS, USER, BIRTH, BIO);
        Assert.assertEquals("notEqualPass", obj.getString("error"));
    }

    @Test
    public void testOK() {
        JSONObject obj = new UserRequests().signup(NICK, MAIL, PASS, PASS, USER, BIRTH, BIO);
        Assert.assertEquals("ok", obj.getString("error"));
    }

}
