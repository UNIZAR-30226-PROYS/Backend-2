package es.eina.sql.entities;

import es.eina.TestBase;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.utils.UserUtils;
import org.junit.*;

import java.sql.Date;

public class FollowersTest extends TestBase {

    private EntityUser user1;
    private EntityUser user2;
    private EntityUser user3;
    private EntityUser user4;
    private EntityUser user5;

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
        user1 = UserUtils.addUser("test-user1", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user2 = UserUtils.addUser("test-user2", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user3 = UserUtils.addUser("test-user3", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user4 = UserUtils.addUser("test-user4", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        user5 = UserUtils.addUser("test-user5", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
    }

    @After
    public void endTest() {
        UserCache.deleteUser(user1);
        UserCache.deleteUser(user2);
        UserCache.deleteUser(user3);
        UserCache.deleteUser(user4);
        UserCache.deleteUser(user5);
    }

    @Test
    public void testFollowPersistence() {
        Assert.assertTrue(user1.followUser(user2));
        Assert.assertEquals(1, SQLUtils.getRowCountSQL("user_followers", "follower = " + user2.getId() + " and followee = " + user1.getId()));
        Assert.assertEquals(0, SQLUtils.getRowCountSQL("user_followers", "follower = " + user1.getId() + " and followee = " + user2.getId()));
    }

    @Test
    public void testFollowOnlyOne(){
        Assert.assertTrue(user1.followUser(user2));
        Assert.assertTrue(user2.getFollowers().contains(user1));
        Assert.assertTrue(user1.getFollowees().contains(user2));
    }

}
