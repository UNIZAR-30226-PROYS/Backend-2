package es.eina;

import es.eina.cache.AlbumCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.sql.utils.HibernateUtilsTest;
import es.eina.utils.AlbumUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TestBase {

    private List<EntityUser> users = new ArrayList<>();
    private List<EntitySong> songs = new ArrayList<>();
    private List<EntityAlbum> albums = new ArrayList<>();

    protected Session s;
    protected Transaction t;
    private boolean sessionOpen;

    protected void openSession(){
        if(!sessionOpen) {
            s = HibernateUtils.getSession();
            t = s.beginTransaction();
            sessionOpen = true;
        }
    }

    protected void closeSession(){
        if(sessionOpen) {
            t.commit();
            s.close();
            sessionOpen = false;
        }
    }

    protected static void openDB(){
        InputStream f = TestBase.class.getResourceAsStream("/database-test.properties");
        System.out.println(TestBase.class.getResource("/"));
        //InputStream f = HibernateUtilsTest.class.getResourceAsStream("/database-test.properties");
        Assert.assertNotNull(f);
        SessionFactory session = HibernateUtils.configureDatabase(f);
        Assert.assertNotNull(session);
    }

    protected static void closeDB() {
        HibernateUtils.shutdown();
    }

    protected JSONObject performTest(String test){
        return new JSONObject(test);
    }

    protected boolean contains(JSONArray songs, Long id) {
        boolean has = false;
        int i = 0;
        while(!has && i < songs.length()){
            has = songs.getLong(i) == id;
            i++;
        }
        return has;
    }

    protected boolean containsID(JSONArray songs, Long id) {
        boolean has = false;
        int i = 0;
        while(!has && i < songs.length()){
            has = songs.getJSONObject(i).getLong("id") == id;
            i++;
        }
        return has;
    }

    protected void createUsers(int amount){
        if(!sessionOpen) openSession();
        for(int i = 0; i < amount; i++){
            users.add(UserUtils.addUser(s, "test-user" + i, "a@a.es", "123456", "Username", "Bio:D", new Date(0), "O1"));
        }
    }

    protected void createSongs(int amount, EntityAlbum defaultAlbum){
        if(!sessionOpen) openSession();
        for(int i = 0; i < amount; i++){
            songs.add(SongUtils.addSong(s, defaultAlbum, "Song " + i, "O1"));
        }
    }

    protected void createAlbums(int amount, EntityUser defaultUser){
        if(!sessionOpen) openSession();
        for(int i = 0; i < amount; i++){
            albums.add(AlbumUtils.createAlbum(s, defaultUser, "Album " + i, 1970));
        }
    }

    protected void deleteUsers(){
        if(!sessionOpen) openSession();
        for(EntityUser user : users){
            UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        }
    }

    protected void deleteSongs(){
        if(!sessionOpen) openSession();
        for(EntitySong user : songs){
            SongCache.deleteSong(s, SongCache.getSong(s, user.getId()));
        }
    }

    protected void deleteAlbums(){
        if(!sessionOpen) openSession();
        for(EntityAlbum user : albums){
            AlbumCache.deleteAlbum(s, AlbumCache.getAlbum(s, user.getAlbumId()));
        }
    }

    protected void saveUsers(){
        if(!sessionOpen) openSession();
        for(EntityUser user : users){
            UserCache.deleteUser(s, UserCache.getUser(s, user.getId()));
        }
    }

    protected void saveSongs(){
        if(!sessionOpen) openSession();
        for(EntitySong user : songs){
            SongCache.deleteSong(s, SongCache.getSong(s, user.getId()));
        }
    }

    protected void saveAlbums(){
        if(!sessionOpen) openSession();
        for(EntityAlbum user : albums){
            AlbumCache.deleteAlbum(s, AlbumCache.getAlbum(s, user.getAlbumId()));
        }
    }

    protected EntityUser getUser(int id){
        if(!sessionOpen) openSession();
        return UserCache.getUser(s, users.get(id).getId());
    }

    protected EntitySong getSong(int id){
        if(!sessionOpen) openSession();
        return SongCache.getSong(s, songs.get(id).getId());
    }

    protected EntityAlbum getAlbum(int id){
        if(!sessionOpen) openSession();
        return AlbumCache.getAlbum(s, albums.get(id).getAlbumId());
    }
}
