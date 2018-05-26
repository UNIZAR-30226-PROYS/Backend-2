package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
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

import java.sql.Date;

public class SongGetTest extends TestBase {

    private EntityUser user;
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
        user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        song = SongUtils.addSong(user, "Random Song", "O1");
    }

    @After
    public void endTest() {
        HibernateUtils.deleteFromDB(song);
        HibernateUtils.deleteFromDB(user);
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = new SongRequests().getSong(-1);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownSong() {
        JSONObject obj = new SongRequests().getSong(Long.MAX_VALUE);
        Assert.assertEquals("unknownSong", obj.getString("error"));
    }

    @Test
    public void testOK() {
        JSONObject obj = new SongRequests().getSong(song.getId());
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals((long)song.getId(), obj.getJSONObject("song").getLong("id"));
        Assert.assertEquals((long)user.getId(), obj.getJSONObject("song").getLong("user_id"));
    }
}
