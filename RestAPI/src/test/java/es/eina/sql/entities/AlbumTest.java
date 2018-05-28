package es.eina.sql.entities;

import es.eina.TestBase;
import es.eina.cache.AlbumCache;
import es.eina.cache.UserCache;
import es.eina.crypt.Crypter;
import es.eina.utils.AlbumUtils;
import es.eina.utils.UserUtils;
import org.junit.*;

import java.sql.Date;

public class AlbumTest extends TestBase {

    private EntityUser user;
    private EntityAlbum album0;

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
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        closeSession();
    }

    @Test
    public void testCreateAlbum(){
        openSession();
        EntityAlbum album = new EntityAlbum(user, "RandomTitle", 1970);
        Assert.assertTrue(AlbumCache.addAlbum(s, album));
        AlbumCache.deleteAlbum(s, album);
        closeSession();
    }

    @Test
    public void testDeleteAlbum(){
        openSession();
        EntityAlbum album = new EntityAlbum(user, "RandomTitle", 1970);
        Assert.assertTrue(AlbumCache.addAlbum(s, album));
        closeSession();
    }

}
