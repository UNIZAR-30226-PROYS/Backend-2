package es.eina.sql.entities;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.requests.AlbumRequests;
import es.eina.sql.SQLUtils;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;
import org.slf4j.LoggerFactory;

import java.sql.Date;

public class PersistenceTest extends TestBase {

    private EntityUser user;
    private EntityAlbum album;
    private EntitySong song;

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
        //user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
    }

    @After
    public void endTest() {

    }

    @Test
    public void testCreateDeleteUser() {
        System.out.println(">>>>>>>>>>>><<<<<<<<<<<<<<<<");
        LoggerFactory.getLogger(PersistenceTest.class).debug("Init Persistance Test");
        System.out.println(">>>>>>>>>>>><<<<<<<<<<<<<<<<");
        openSession();
        user = UserUtils.addUser(s, "test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        closeSession();

        Assert.assertNotNull(user);

        openSession();
        Assert.assertEquals(1, SQLUtils.getRowCountSQL(s,"users", "nick = '" + user.getNick() + "'"));
        Assert.assertEquals(1, SQLUtils.getRowCountSQL(s, "sessions", "user_id = '" + user.getId() + "'"));

        String nick = user.getNick();
        long id = user.getId();
        UserCache.deleteUser(s, user);
        closeSession();

        openSession();
        Assert.assertEquals(0, SQLUtils.getRowCountSQL(s, "users", "nick = '" + nick + "'"));
        Assert.assertEquals(0, SQLUtils.getRowCountSQL(s, "sessions", "user_id = '" + id + "'"));
        closeSession();
    }

}
