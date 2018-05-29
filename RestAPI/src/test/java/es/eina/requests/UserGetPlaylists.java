package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.SongListsUtils;
import es.eina.utils.UserUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserGetPlaylists extends TestBase {

    private EntityUser user, user2;
    private EntitySongList list1, list2, list3;

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
        user = UserUtils.addUser(s,"test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user2 = UserUtils.addUser(s,"test2-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        list1 = SongListsUtils.addList(s, "List1", user);
        list2 = SongListsUtils.addList(s, "List1", user2);
        list3 = SongListsUtils.addList(s, "List1", user);
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list1.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list2.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list3.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user2.getId()));
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new UserSongListRequests().getLists(""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserSongListRequests().getLists((String)null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new UserSongListRequests().getLists("invalid-user"));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testOK() {
        JSONObject obj = performTest(new UserSongListRequests().getLists(user.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(2, obj.getInt("size"));
        JSONArray array = obj.getJSONArray("lists");
        Assert.assertEquals(2, array.length());
        Assert.assertTrue(contains(array, list1.getId()));
        Assert.assertFalse(contains(array, list2.getId()));
        Assert.assertTrue(contains(array, list3.getId()));

        obj = performTest(new UserSongListRequests().getLists(user2.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("size"));
        array = obj.getJSONArray("lists");
        Assert.assertEquals(1, array.length());
        Assert.assertFalse(contains(array, list1.getId()));
        Assert.assertTrue(contains(array, list2.getId()));
        Assert.assertFalse(contains(array, list3.getId()));
    }

}
