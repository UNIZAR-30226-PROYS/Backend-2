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
        user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
    }

    @After
    public void endTest() {
        UserCache.deleteUser(user);
    }

    @Test
    public void testCreateAlbum(){
        EntityAlbum album = new EntityAlbum(user, "RandomTitle", 1970);
        Assert.assertTrue(AlbumCache.addAlbum(album));

        Assert.assertTrue(UserUtils.userExists("random_user"));
        Assert.assertTrue(!UserUtils.userExists("invalid_user"));
        Assert.assertTrue(UserUtils.checkPassword(user, "123456"));

        Assert.assertTrue(UserCache.deleteUser(user));
        Assert.assertTrue(!UserUtils.userExists("random_user"));
    }

    @Test
    public void testDeleteAlbum(){
        EntityAlbum album = new EntityAlbum(user, "RandomTitle", 1970);
        Assert.assertTrue(AlbumCache.addAlbum(album));
    }

}
