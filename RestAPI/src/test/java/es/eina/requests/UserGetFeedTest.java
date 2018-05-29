package es.eina.requests;

import es.eina.TestBase;
import org.json.JSONObject;
import org.junit.*;

public class UserGetFeedTest extends TestBase {
/*
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
        createUsers(20);
        createAlbums(2, getUser(0));
        createAlbums(2, getUser(3));
        createAlbums(2, getUser(4));
        createSongs(4, getAlbum(0));
        createSongs(3, getAlbum(2));
        getUser(0).followUser(getUser(1));
        closeSession();
    }

    @After
    public void endTest() {
        openSession();
        deleteSongs();
        deleteAlbums();
        deleteUsers();
        closeSession();
    }

    @Test
    public void testFeedInvalidArgs(){
        JSONObject obj = performTest(new UserRequests().getFeed(getUser(0).getNick(), 0, 0, 0));
        Assert.assertEquals("invalidArgs", obj.getString("error"));
    }

    @Test
    public void testFeedUnknownUser(){
        JSONObject obj = performTest(new UserRequests().getFeed("invalid-user", 2, 2, 2));
        Assert.assertEquals("unknownUser", obj.getString("error"));
    }

    @Test
    public void testGetLastUploadedSongs(){
        JSONObject obj = performTest(new UserRequests().getFeed(getUser(0).getNick(), 0, 0, 2));
        Assert.assertTrue(obj.getJSONObject("songs").getLong("amount") <= 2);
        Assert.assertTrue(containsID(obj.getJSONObject("songs").getJSONArray("data"), ));
    }

    @Test
    public void testGetPopularSongs() {
        JSONObject obj = performTest(new UserRequests().getFeed(1, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 1);
        Assert.assertTrue(
                containsID(obj.getJSONArray("songs"), getSong(0).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(1).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(2).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(3).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(4).getId())
        );

        obj = performTest(new SongRequests().getPopularSongs(10, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 10);
        for(int i = 0; i < 5; i++)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

        obj = performTest(new SongRequests().getPopularSongs(20, false));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 20);
        for(int i = 0; i < 10; i++)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

    }

    @Test
    public void testGetCountryPopularSongs() {
        JSONObject obj = performTest(new SongRequests().getPopularSongs("SP", 1, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 1);
        Assert.assertTrue(
                containsID(obj.getJSONArray("songs"), getSong(0).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(2).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(4).getId())
        );

        obj = performTest(new SongRequests().getPopularSongs("US", 1, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 1);
        Assert.assertTrue(
                        containsID(obj.getJSONArray("songs"), getSong(1).getId()) ||
                        containsID(obj.getJSONArray("songs"), getSong(3).getId())
        );

        obj = performTest(new SongRequests().getPopularSongs("SP", 10, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 10);
        for(int i = 0; i < 5; i+=2)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

        obj = performTest(new SongRequests().getPopularSongs("US", 10, true));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 10);
        for(int i = 1; i < 5; i+=2)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

        obj = performTest(new SongRequests().getPopularSongs("SP", 20, false));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 20);
        for(int i = 0; i < 10; i+=2)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

        obj = performTest(new SongRequests().getPopularSongs("US", 20, false));
        Assert.assertEquals("ok", obj.getString("error"));
        Assert.assertTrue(obj.getLong("results") <= 20);
        for(int i = 1; i < 10; i+=2)
            Assert.assertTrue(containsID(obj.getJSONArray("songs"), getSong(i).getId()));

    }*/
}
