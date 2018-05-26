package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.*;

import java.sql.Date;

public class AlbumRemoveSongTest extends TestBase {

    private EntityUser user;
    private EntityAlbum album;
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
        user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        album = AlbumUtils.createAlbum(user, "Random Album", 1900);
        song = SongUtils.addSong(album, "Random Song", "O1");
    }

    @After
    public void endTest() {
        HibernateUtils.deleteFromDB(album);
        HibernateUtils.deleteFromDB(song);
        HibernateUtils.deleteFromDB(user);
    }

    @Test
    public void testErrorsInvalidArgs() {
        JSONObject obj = new AlbumRequests().removeSongFromAlbum("", user.getToken().getToken(), album.getAlbumId(), song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new AlbumRequests().removeSongFromAlbum(null, user.getToken().getToken(), album.getAlbumId(), song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));

        obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), "", album.getAlbumId(), song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
        obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), null, album.getAlbumId(), song.getId());
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownUser() {
        JSONObject obj = new AlbumRequests().removeSongFromAlbum("invalid-user", user.getToken().getToken(), album.getAlbumId(), song.getId());
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidToken() {
        JSONObject obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), "invalid+" + user.getToken().getToken(), album.getAlbumId(), song.getId());
        Assert.assertEquals("invalidToken", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidAlbum() {
        JSONObject obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), user.getToken().getToken(), -1, song.getId());
        Assert.assertEquals("invalidAlbum", obj.getString("error"));
    }
    @Test
    public void testErrorsUnknownAlbum() {
        JSONObject obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), user.getToken().getToken(), Long.MAX_VALUE, song.getId());
        Assert.assertEquals("unknownAlbum", obj.getString("error"));
    }

    @Test
    public void testErrorsInvalidSong() {
        JSONObject obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), user.getToken().getToken(), album.getAlbumId(), -1);
        Assert.assertEquals("invalidSong", obj.getString("error"));
    }

    @Test
    public void testErrorsUnknownSong() {
        JSONObject obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), user.getToken().getToken(), album.getAlbumId(), Long.MAX_VALUE);
        Assert.assertEquals("unknownSong", obj.getString("error"));
    }

    @Test
    public void testErrorsNotAuthor() {
        EntityUser second = UserUtils.addUser("second-user", "a@a.es", "1234", "SecUser", "", new Date(0), "O1");
        Assert.assertNotNull(second);
        JSONObject obj = new AlbumRequests().removeSongFromAlbum(second.getNick(), second.getToken().getToken(), album.getAlbumId(), song.getId());
        Assert.assertEquals("notAuthor", obj.getString("error"));
        UserCache.deleteUser(second);
    }

    @Test
    public void testOK() {

        JSONObject obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), user.getToken().getToken(), album.getAlbumId(), song.getId());

        Assert.assertEquals(0, SQLUtils.getRowCount("song", "id = " + song.getId() + " and album_id = " + album.getAlbumId()));
        Assert.assertEquals("ok", obj.getString("error"));
    }

    @Test
    public void testAlreadyAdded() {

        JSONObject obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), user.getToken().getToken(), album.getAlbumId(), song.getId());
        Assert.assertEquals("ok", obj.getString("error"));

        obj = new AlbumRequests().removeSongFromAlbum(user.getNick(), user.getToken().getToken(), album.getAlbumId(), song.getId());
        Assert.assertEquals("alreadyRemoved", obj.getString("error"));
    }
}
