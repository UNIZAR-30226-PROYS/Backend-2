package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.SongListsUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class PlaylistGetTest extends TestBase {

    private EntityUser user;
    private EntitySongList list;

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
        openSession();
        user = UserUtils.addUser(s,"test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        list = SongListsUtils.addList(s, "Title", user);
        closeSession();
    }

    @After
    public void endTest(){
        openSession();
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list.getId()));
        closeSession();
    }

    @Test
    public void testErrorsInvalidList(){
        JSONObject obj = performTest(new UserSongListRequests().getLists(-1L));
        Assert.assertEquals("invalidList", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownList(){
        JSONObject obj = performTest(new UserSongListRequests().getLists(Long.MAX_VALUE));
        Assert.assertEquals("unknownList", obj.getString("error"));
    }

    @Test
    public void testErrorsOK(){
        JSONObject obj = performTest(new UserSongListRequests().getLists(list.getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals((long)list.getId(), obj.getJSONObject("list").getLong("id"));
        Assert.assertEquals((long)user.getId(), obj.getJSONObject("list").getLong("author"));
    }

}
