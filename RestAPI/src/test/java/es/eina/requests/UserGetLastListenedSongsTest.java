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
        openSession();
        user = UserUtils.addUser(s, "test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user2 = UserUtils.addUser(s, "test-user2", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        album = AlbumUtils.createAlbum(s, user, "Random Album", 1900);
        album2 = AlbumUtils.createAlbum(s, user2, "Random Album", 1900);
        song = SongUtils.addSong(s, album, "Song1", "ES");
        song2 = SongUtils.addSong(s, album, "Song2", "ES");
        song3 = SongUtils.addSong(s, album, "Song3", "ES");
    }

    @After
    public void endTest() {
        openSession();
        SongCache.deleteSong(s, SongCache.getSong(s, song.getId()));
        SongCache.deleteSong(s, SongCache.getSong(s, song2.getId()));
        SongCache.deleteSong(s, SongCache.getSong(s, song3.getId()));
        AlbumCache.deleteAlbum(s, AlbumCache.getAlbum(s, album.getAlbumId()));
        AlbumCache.deleteAlbum(s, AlbumCache.getAlbum(s, album2.getAlbumId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user2.getId()));
        closeSession();
    }

    @Test
    public void testInvalidArgs(){
        closeSession();
        JSONObject obj = performTest(new UserRequests().getLastListenedSongs("", 1));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserRequests().getLastListenedSongs(null, 1));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testInvalidAmount(){
        closeSession();
        JSONObject obj = performTest(new UserRequests().getLastListenedSongs(user.getNick(), 0));
        Assert.assertEquals("invalidAmount", obj.getString("error"));
    }

    @Test
    public void testUnknownUser(){
        closeSession();
        JSONObject obj = performTest(new UserRequests().getLastListenedSongs("invalid-user", 1));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testOK() throws InterruptedException {
        user.listenSong(song3);
        Thread.sleep(1);
        user.listenSong(song);
        Thread.sleep(1);
        user.listenSong(song2);
        Thread.sleep(1);
        user.listenSong(song);
        closeSession();
        JSONObject obj = performTest(new UserRequests().getLastListenedSongs(user.getNick(), 1));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(1, obj.getInt("size"));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(0));

        obj = performTest(new UserRequests().getLastListenedSongs(user.getNick(), 4));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(4, obj.getInt("size"));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(0));
        Assert.assertEquals((long)song2.getId(), obj.getJSONArray("songs").getLong(1));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(2));
        Assert.assertEquals((long)song3.getId(), obj.getJSONArray("songs").getLong(3));

        obj = performTest(new UserRequests().getLastListenedSongs(user.getNick(), 10));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(4, obj.getInt("size"));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(0));
        Assert.assertEquals((long)song2.getId(), obj.getJSONArray("songs").getLong(1));
        Assert.assertEquals((long)song.getId(), obj.getJSONArray("songs").getLong(2));
        Assert.assertEquals((long)song3.getId(), obj.getJSONArray("songs").getLong(3));
    }
}
