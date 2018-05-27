package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class SongHasFavedTest extends TestBase {

    private EntityUser user;
    private EntityUser user2;
    private EntityAlbum album;
    private EntitySong song;
    private EntitySong song2;
    private EntitySong song3;
    private EntitySong song4;
    private EntitySong song5;

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
        album = AlbumUtils.createAlbum(s, user, "Random Album", 1900);
        song = SongUtils.addSong(s, album, "Random Song", "O1");
        song2 = SongUtils.addSong(s, album, "Random Song", "O1");
        song3 = SongUtils.addSong(s, album, "Random Song", "O1");
        song4 = SongUtils.addSong(s, album, "Random Song", "O1");
        song5 = SongUtils.addSong(s, album, "Random Song", "O1");
        user.favSong(song);
        user.favSong(song2);
        user.favSong(song4);
        user2.favSong(song4);
        user2.favSong(song5);
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        SongCache.deleteSong(s, song);
        SongCache.deleteSong(s, song2);
        SongCache.deleteSong(s, song3);
        SongCache.deleteSong(s, song4);
        SongCache.deleteSong(s, song5);
        AlbumCache.deleteAlbum(s, album);
        UserCache.deleteUser(s, user);
        UserCache.deleteUser(s, user2);
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = new SongRequests().hasFaved("", song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new SongRequests().hasFaved(null, song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = new SongRequests().hasFaved("invalid-user", song.getId());
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownSong() {
        JSONObject obj = new SongRequests().hasFaved(user.getNick(), -1L);
        Assert.assertEquals("unknownSong", obj.getString("error"));
        obj = new SongRequests().hasFaved(user.getNick(), Long.MAX_VALUE);
        Assert.assertEquals("unknownSong", obj.getString("error"));
    }

    @Test
    public void testErrorsNoFav() {
        JSONObject obj = new SongRequests().hasFaved(user.getNick(), song3.getId());
        Assert.assertEquals("noFav", obj.getString("error"));
        obj = new SongRequests().hasFaved(user2.getNick(), song2.getId());
        Assert.assertEquals("noFav", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = new SongRequests().hasFaved(user.getNick(), song2.getId());
        Assert.assertEquals("ok", obj.getString("error"));
        obj = new SongRequests().hasFaved(user2.getNick(), song4.getId());
        Assert.assertEquals("ok", obj.getString("error"));
    }

}
