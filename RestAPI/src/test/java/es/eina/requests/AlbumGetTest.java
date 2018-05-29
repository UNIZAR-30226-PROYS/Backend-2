package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.AlbumUtils;
import es.eina.utils.UserUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;

public class AlbumGetTest extends TestBase {

    private static final Logger LOG = LoggerFactory.getLogger(AlbumGetTest.class);

    private EntityUser user;
    private EntityAlbum album;

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
        album = AlbumUtils.createAlbum(s, user, "Random Album", 1900);
        closeSession();
        LOG.info("START TEST!!");
    }

    @After
    public void endTest() {
        LOG.info("END TEST!!");
        openSession();
        AlbumCache.deleteAlbum(s, AlbumCache.getAlbum(s, album.getAlbumId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        closeSession();
    }

    @Test
    public void testInvalidArgs(){
        JSONObject obj = performTest(new AlbumRequests().get(0));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new AlbumRequests().get(-1));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testUnknownAlbum(){
        JSONObject obj = performTest(new AlbumRequests().get(Integer.MAX_VALUE));
        Assert.assertEquals("unknownAlbum", obj.getString("error"));
    }

    @Test
    public void testOK(){
        JSONObject obj = performTest(new AlbumRequests().get(album.getAlbumId()));
        Assert.assertEquals("ok", obj.getString("error"));

        JSONObject expected = album.toJSON();
        JSONObject returned = obj.getJSONObject("album");
        JSONAssert.assertEquals(expected, returned, true);
    }


}
