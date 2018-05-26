package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserReproduceSongTest extends TestBase {

    private EntityUser user;
    private EntityAlbum album;
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
        album = AlbumUtils.createAlbum(user, "Title", 1970);
        song = SongUtils.addSong(album, "Song 1", "ES");
    }

    @After
    public void endTest(){
        UserCache.deleteUser(user);
        AlbumCache.deleteAlbum(album);
    }

    @Test
    public void testErrorsInvalidArgs(){
        JSONObject obj = new SongRequests().listenSong("", user.getToken().getToken(), song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new SongRequests().listenSong(null, user.getToken().getToken(), song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new SongRequests().listenSong(user.getNick(), "", song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new SongRequests().listenSong(user.getNick(), null, song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));

    }

    @Test
    public void testErrorsUnknownUser(){
        JSONObject obj = new SongRequests().listenSong("invalid-user", user.getToken().getToken(), song.getId());
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken(){
        JSONObject obj = new SongRequests().listenSong(user.getNick(), "invalid+" + user.getToken().getToken(), song.getId());
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownSong(){
        JSONObject obj = new SongRequests().listenSong(user.getNick(), user.getToken().getToken(), -1);
        Assert.assertEquals("unknownSong", obj.getString("error"));
        obj = new SongRequests().listenSong(user.getNick(), user.getToken().getToken(), Integer.MAX_VALUE);
        Assert.assertEquals("unknownSong", obj.getString("error"));
    }

    @Test
    public void testOK(){

        JSONObject obj = new SongRequests().listenSong(user.getNick(), user.getToken().getToken(), song.getId());

        Assert.assertEquals(1, SQLUtils.getRowCountSQL("user_listened_songs", "user_id = " + user.getId() + " and song_id = " + song.getId()));
        Assert.assertEquals("ok", obj.getString("error"));
    }
}
