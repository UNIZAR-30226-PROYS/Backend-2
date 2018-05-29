package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.SongCache;
import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.SongListsUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class PlaylistAddSongTest extends TestBase {

    private EntityUser user;
    private EntitySongList list;
    private EntitySong song;

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
        list = SongListsUtils.addList(s, "List id", user);
        song = SongUtils.addSong(s, null, "Random Song", "O1");
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        SongCache.deleteSong(s, SongCache.getSong(s, song.getId()));
        SongListCache.deleteSongList(s, SongListCache.getSongList(s, list.getId()));
        UserCache.deleteUser(s, UserCache.getUser(s, user.getNick()));
        closeSession();
    }

    //JSONObject obj = performTest(new UserSongListRequests().add(user.getNick(), user.getToken().getToken(), list.getId(), song.getId()));
    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = performTest(new UserSongListRequests().add("", user.getToken().getToken(), list.getId(), song.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserSongListRequests().add(null, user.getToken().getToken(), list.getId(), song.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = performTest(new UserSongListRequests().add(user.getNick(), "", list.getId(), song.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = performTest(new UserSongListRequests().add(user.getNick(), null, list.getId(), song.getId()));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = performTest(new UserSongListRequests().add("invalid-user", user.getToken().getToken(), list.getId(), song.getId()));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken() {
        JSONObject obj = performTest(new UserSongListRequests().add(user.getNick(), "invalid-token", list.getId(), song.getId()));
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownList() {
        JSONObject obj = performTest(new UserSongListRequests().add(user.getNick(), user.getToken().getToken(), Long.MAX_VALUE, song.getId()));
        Assert.assertEquals("unknownList", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownSong() {
        JSONObject obj = performTest(new UserSongListRequests().add(user.getNick(), user.getToken().getToken(), list.getId(), Long.MAX_VALUE));
        Assert.assertEquals("unknownSong", obj.getString("error"));
    }

    @Test
    public void testErrorsNotAuthor() {
        openSession();
        EntityUser second = UserUtils.addUser(s, "second-user", "a@a.es", "1234", "SecUser", "", new Date(0), "O1");
        closeSession();
        Assert.assertNotNull(second);

        JSONObject obj = performTest(new UserSongListRequests().add(second.getNick(), second.getToken().getToken(), list.getId(), song.getId()));
        Assert.assertEquals("notAuthor", obj.getString("error"));

        openSession();
        UserCache.deleteUser(s, second);
        closeSession();
    }

    @Test
    public void testOK() {

        JSONObject obj = performTest(new UserSongListRequests().add(user.getNick(), user.getToken().getToken(), list.getId(), song.getId()));

        openSession();
        Assert.assertEquals(1, SQLUtils.getRowCountSQL(s, "song_list_songs", "song_id = " + song.getId() + " and list_id = " + list.getId()));
        Assert.assertEquals("ok", obj.getString("error"));
        closeSession();
    }

    @Test
    public void testAlreadyAdded() {

        JSONObject obj = performTest(new UserSongListRequests().add(user.getNick(), user.getToken().getToken(), list.getId(), song.getId()));
        Assert.assertEquals("ok", obj.getString("error"));

        obj = performTest(new UserSongListRequests().add(user.getNick(), user.getToken().getToken(), list.getId(), song.getId()));
        Assert.assertEquals("alreadyAdded", obj.getString("error"));
    }
}
