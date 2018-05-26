package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class UserGetSongs extends TestBase {

    private EntityUser user, user2;
    private EntityAlbum album, album2;
    private EntitySong song1, song2, song3;

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
        user2 = UserUtils.addUser("test2-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        album = AlbumUtils.createAlbum(user, "Album1", 1970);
        album2 = AlbumUtils.createAlbum(user2, "Album2", 1970);
        song1 = SongUtils.addSong(album, "Song1", "O1");
        song2 = SongUtils.addSong(album2, "Song2", "O1");
        song3 = SongUtils.addSong(album, "Song3", "O1");
    }

    @After
    public void endTest() {
        SongCache.deleteSong(song1);
        SongCache.deleteSong(song2);
        SongCache.deleteSong(song3);
        AlbumCache.deleteAlbum(album);
        AlbumCache.deleteAlbum(album2);
        UserCache.deleteUser(user);
        UserCache.deleteUser(user2);
    }

    public boolean contains(JSONArray array, Long id){
        boolean x = false;
        int i = 0;
        while(!x && i < array.length()){
            x = array.get(i).equals(id);
        }

        return x;
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = new UserRequests().getUserSongs("");
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new UserRequests().getUserSongs(null);
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = new UserRequests().getUserSongs("invalid-user");
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testOK() {
        JSONObject obj = new UserRequests().getUserSongs(user.getNick());
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertEquals(2, obj.getInt("size"));
        JSONArray array = obj.getJSONArray("songs");
        Assert.assertEquals(2, array.length());
        Assert.assertTrue(contains(array, song1.getId()));
        Assert.assertFalse(contains(array, song2.getId()));
        Assert.assertTrue(contains(array, song3.getId()));
    }

}
