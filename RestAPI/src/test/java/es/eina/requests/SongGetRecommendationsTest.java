package es.eina.requests;

import es.eina.RestApp;
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

public class SongGetRecommendationsTest extends TestBase {
/*
    private EntityUser user;
    private EntityUser user2;
    private EntityAlbum album;
    private EntitySong song;
    private EntitySong song2;
    private EntitySong song3;
    private EntitySong song4;
    private EntitySong song5;
    private RestApp r;

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

        r = new RestApp();
        Thread.sleep(2000);
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
        JSONObject obj = performTest(new SongRequests().getSongRecommendation(-1, 10));
        //Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownSong() {
        JSONObject obj = performTest(new SongRequests().getSongRecommendation(Long.MAX_VALUE, 10));
        //Assert.assertEquals("unknownSong", obj.getString("error"));
        //Assert.assertEquals(0, obj.getInt("amount"));
    }

    @Test
    public void testErrorsOK() {
        JSONObject obj = performTest(new SongRequests().getSongRecommendation(song.getId(), 10));
        //Assert.assertEquals("ok", obj.getString("error"));
        //Assert.assertEquals(1, obj.getInt("amount"));
    }
    */

}
