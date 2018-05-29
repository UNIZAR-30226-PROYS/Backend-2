package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.SongListsUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserUnfollowPlaylist extends TestBase {

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
        list = SongListsUtils.addList(s, "List 1", user);
        list.addfollower(user);
        closeSession();
    }

    @After
    public void endTest(){
        openSession();
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        closeSession();
    }

    //JSONObject obj = performTest(new UserSongListRequests().removefollower(user.getNick(), user.getToken().getToken(), list.getId()));

    @Test
    public void testErrorsInvalidArgs(){
        JSONObject obj = performTest(new UserSongListRequests().removefollower("", user.getToken().getToken(), list.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserSongListRequests().removefollower(null, user.getToken().getToken(), list.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new UserSongListRequests().removefollower(user.getNick(), "", list.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserSongListRequests().removefollower(user.getNick(), null, list.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

    }

    @Test
    public void testErrorsUnknownUser(){
        JSONObject obj = performTest(new UserSongListRequests().removefollower("invalid-user", user.getToken().getToken(), list.getId()));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken(){
        JSONObject obj = performTest(new UserSongListRequests().removefollower(user.getNick(), "invalid-token", list.getId()));
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownList(){
        JSONObject obj = performTest(new UserSongListRequests().removefollower(user.getNick(), user.getToken().getToken(), -1L));
        Assert.assertEquals("unknownList", obj.getString("error"));
        obj = performTest(new UserSongListRequests().removefollower(user.getNick(), user.getToken().getToken(), Long.MAX_VALUE));
        Assert.assertEquals("unknownList", obj.getString("error"));
    }

    @Test
    public void testErrorsAlreadyAdded(){
        JSONObject obj = performTest(new UserSongListRequests().removefollower(user.getNick(), user.getToken().getToken(), list.getId()));

        openSession();
        Assert.assertEquals(0, SQLUtils.getRowCountSQL(s,"song_list_user_follows", "user_id = " + user.getId() + " and song_list_id = " + list.getId()));
        closeSession();
        Assert.assertEquals("ok", obj.getString("error"));

        obj = performTest(new UserSongListRequests().removefollower(user.getNick(), user.getToken().getToken(), list.getId()));
        Assert.assertEquals("alreadyRemoved", obj.getString("error"));
    }

    @Test
    public void testErrorsOK(){
        JSONObject obj = performTest(new UserSongListRequests().removefollower(user.getNick(), user.getToken().getToken(), list.getId()));

        openSession();
        Assert.assertEquals(0, SQLUtils.getRowCountSQL(s,"song_list_user_follows", "user_id = " + user.getId() + " and song_list_id = " + list.getId()));
        closeSession();
        Assert.assertEquals("ok", obj.getString("error"));
    }
}
