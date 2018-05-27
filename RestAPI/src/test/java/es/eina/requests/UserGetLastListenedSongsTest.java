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

public class UserGetLastListenedSongsTest extends TestBase {

    private EntityUser user, user2;
    private EntityAlbum album, album2;
    private EntitySong song, song2, song3;

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
        user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user2 = UserUtils.addUser("test-user2", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        album = AlbumUtils.createAlbum(user, "Random Album", 1900);
        album2 = AlbumUtils.createAlbum(user2, "Random Album", 1900);
        song = SongUtils.addSong(album, "Song1", "ES");
        song2 = SongUtils.addSong(album, "Song2", "ES");
        song3 = SongUtils.addSong(album, "Song3", "ES");
    }

    @After
    public void endTest() {
        SongCache.deleteSong(song);
        SongCache.deleteSong(song2);
        SongCache.deleteSong(song3);
        AlbumCache.deleteAlbum(album);
        AlbumCache.deleteAlbum(album2);
        UserCache.deleteUser(user);
        UserCache.deleteUser(user2);
    }

    @Test
    public void testInvalidArgs(){
        JSONObject obj = new UserRequests().getLastListenedSongs("", 1);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().getLastListenedSongs(null, 1);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testInvalidAmount(){
        JSONObject obj = new UserRequests().getLastListenedSongs(user.getNick(), 0);
        Assert.assertEquals("invalidAmount", obj.getString("error"));
    }

    @Test
    public void testUnknownUser(){
        JSONObject obj = new UserRequests().getLastListenedSongs("invalid-user", 1);
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testOK(){
        user.listenSong(song3);
        user.listenSong(song);
        user.listenSong(song2);
        user.listenSong(song);
        JSONObject obj = new UserRequests().getLastListenedSongs(user.getNick(), 1);
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("size"));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(0));

        obj = new UserRequests().getLastListenedSongs(user.getNick(), 4);
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(4, obj.getInt("size"));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(0));
        Assert.assertEquals((long)song2.getId(), obj.getJSONArray("songs").getLong(1));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(2));
        Assert.assertEquals((long)song3.getId(), obj.getJSONArray("songs").getLong(3));

        obj = new UserRequests().getLastListenedSongs(user.getNick(), 10);
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(4, obj.getInt("size"));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(0));
        Assert.assertEquals((long)song2.getId(), obj.getJSONArray("songs").getLong(1));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(2));
        Assert.assertEquals((long)song3.getId(), obj.getJSONArray("songs").getLong(3));
    }
}
