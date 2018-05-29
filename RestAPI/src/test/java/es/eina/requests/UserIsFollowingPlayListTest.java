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

public class UserIsFollowingPlayListTest extends TestBase {

    private EntityUser user;
    private EntityUser user2;
    private EntitySongList list;

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
        user2 = UserUtils.addUser(s, "test-user2", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        list = SongListsUtils.addList(s, "List", user);
        list.addfollower(user);
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user2.getId()));
        closeSession();
    }

    //JSONObject obj = performTest(new UserSongListRequests().isFollowingList(list.getId(), user.getNick()));
    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new UserSongListRequests().isFollowingList(list.getId(), ""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserSongListRequests().isFollowingList(list.getId(), null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidList() {
        JSONObject obj = performTest(new UserSongListRequests().isFollowingList(-1L, user.getNick()));
        Assert.assertEquals("invalidList", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new UserSongListRequests().isFollowingList(list.getId(), "invalid-user"));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownList() {
        JSONObject obj = performTest(new UserSongListRequests().isFollowingList(Long.MAX_VALUE, user.getNick()));
        Assert.assertEquals("unknownList", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new UserSongListRequests().isFollowingList(list.getId(), user.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));

        obj = performTest(new UserSongListRequests().isFollowingList(list.getId(), user2.getNick()));
        Assert.assertEquals("notFollows", obj.getString("error"));
    }
}
