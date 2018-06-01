package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongListsUtils;
import es.eina.utils.UserUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class GetUserUpdatedFollowedPlaylists extends TestBase {

    private EntityUser user;
    private EntitySongList list;
    private EntitySongList list2;
    private EntitySongList list3;
    private EntitySongList list4;
    private EntitySongList list5;

    @BeforeClass
    public static void start() {
        openDB();
    }

    @AfterClass
    public static void stop() {
        closeDB();
    }

    @Before
    public void setupTest() throws InterruptedException {
        openSession();
        user = UserUtils.addUser(s, "test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        list = SongListsUtils.addList(s, "List1", user);
        Thread.sleep(1005);
        list2 = SongListsUtils.addList(s, "List2", user);
        list3 = SongListsUtils.addList(s, "List3", user);
        list4 = SongListsUtils.addList(s, "List4", user);
        Thread.sleep(1005);
        list5 = SongListsUtils.addList(s, "List5", user);

        list.addfollower(user);
        list5.addfollower(user);
        list4.addfollower(user);
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list2.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list3.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list4.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list5.getId()));
        closeSession();
    }

    //obj = performTest(new UserSongListRequests().getLastUpdatedPlaylists(user.getNick(), 0));
    @Test
    public void testInvalidArgs(){
        JSONObject obj = performTest(new UserSongListRequests().getLastUpdatedPlaylists("", 5));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserSongListRequests().getLastUpdatedPlaylists(null, 5));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testUnknownUser(){
        JSONObject obj = performTest(new UserSongListRequests().getLastUpdatedPlaylists("invalidUser", 5));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testOK(){
        JSONObject obj = performTest(new UserSongListRequests().getLastUpdatedPlaylists(user.getNick(), 1));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("size"));
        Assert.assertFalse(contains(obj.getJSONArray("list"), list.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("list"), list2.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("list"), list3.getId()));
        Assert.assertFalse(contains(obj.getJSONArray("list"), list4.getId()));
        Assert.assertTrue(contains(obj.getJSONArray("list"), list5.getId()));

        obj = performTest(new UserSongListRequests().getLastUpdatedPlaylists(user.getNick(), 10));
        JSONArray array = obj.getJSONArray("list");
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(3, obj.getInt("size"));
        Assert.assertTrue(contains(array, list.getId()));
        Assert.assertFalse(contains(array, list2.getId()));
        Assert.assertFalse(contains(array, list3.getId()));
        Assert.assertTrue(contains(array, list4.getId()));
        Assert.assertTrue(contains(array, list5.getId()));

        Assert.assertEquals(list5.getId(), array.getLong(0));
        Assert.assertEquals(list4.getId(), array.getLong(1));
        Assert.assertEquals(list.getId(), array.getLong(2));
    }

}
