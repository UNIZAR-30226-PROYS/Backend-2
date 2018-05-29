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
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.sql.Date;

public class SongUserGetFavedTest extends TestBase {

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
        SongCache.deleteSong(s, SongCache.getSong(s, song.getId()));
        SongCache.deleteSong(s, SongCache.getSong(s, song2.getId()));
        SongCache.deleteSong(s, SongCache.getSong(s, song3.getId()));
        SongCache.deleteSong(s, SongCache.getSong(s, song4.getId()));
        SongCache.deleteSong(s, SongCache.getSong(s, song5.getId()));
        AlbumCache.deleteAlbum(s, AlbumCache.getAlbum(s, album.getAlbumId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user2.getId()));
        closeSession();
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new SongRequests().getFavedSongs(""));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new SongRequests().getFavedSongs(null));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new SongRequests().getFavedSongs("invalid-user"));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new SongRequests().getFavedSongs(user.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(3, obj.getInt("size"));
        JSONArray songs = obj.getJSONArray("songs");
        Assert.assertTrue(contains(songs, song.getId()));
        Assert.assertTrue(contains(songs, song2.getId()));
        Assert.assertTrue(contains(songs, song4.getId()));

        obj = performTest(new SongRequests().getFavedSongs(user2.getNick()));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(2, obj.getInt("size"));
        songs = obj.getJSONArray("songs");
        Assert.assertTrue(contains(songs, song4.getId()));
        Assert.assertTrue(contains(songs, song5.getId()));
    }

}
