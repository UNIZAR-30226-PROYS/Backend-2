package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserLikeSongTest extends TestBase {

    private EntityUser user;
    private EntitySong song;

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
        user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        Assert.assertNotNull(user);

        song = SongUtils.addSong(user, "Song 1", "ES");
        Assert.assertNotNull(song);
    }

    @After
    public void endTest(){
        Assert.assertTrue(UserCache.deleteUser(user));
    }

    @Test
    public void testErrorsInvalidArgs(){
        JSONObject obj = new SongRequests().likeSong("", user.getToken().getToken(), song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new SongRequests().likeSong(null, user.getToken().getToken(), song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new SongRequests().likeSong(user.getNick(), "", song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new SongRequests().likeSong(user.getNick(), null, song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));

    }

    @Test
    public void testErrorsUnknownUser(){
        Assert.assertNotNull(user);
        Assert.assertNotNull(song);
        JSONObject obj = new SongRequests().likeSong("invalid-user", user.getToken().getToken(), song.getId());
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken(){
        JSONObject obj = new SongRequests().likeSong(user.getNick(), "invalid+" + user.getToken().getToken(), song.getId());
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownSong(){
        JSONObject obj = new SongRequests().likeSong(user.getNick(), user.getToken().getToken(), -1L);
        Assert.assertEquals("unknownSong", obj.getString("error"));
        obj = new SongRequests().likeSong(user.getNick(), user.getToken().getToken(), Long.MAX_VALUE);
        Assert.assertEquals("unknownSong", obj.getString("error"));
    }

    @Test
    public void testAlreadyLiked(){

        JSONObject obj = new SongRequests().likeSong(user.getNick(), user.getToken().getToken(), song.getId());
        Assert.assertEquals("ok", obj.getString("error"));

        obj = new SongRequests().likeSong(user.getNick(), user.getToken().getToken(), song.getId());
        Assert.assertEquals("alreadyLike", obj.getString("error"));
    }

    @Test
    public void testOK(){
        JSONObject obj = new SongRequests().likeSong(user.getNick(), user.getToken().getToken(), song.getId());
        Assert.assertEquals("ok", obj.getString("error"));
    }

}
