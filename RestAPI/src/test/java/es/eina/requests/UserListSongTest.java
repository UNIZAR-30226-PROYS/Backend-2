package es.eina.requests;

import es.eina.TestBase;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.UserUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Date;

public class UserListSongTest extends TestBase {


    public void start(){
        super.openDB();
    }

    public void stop(){
        super.closeDB();
    }

    @Test
    public void testOK(){
        start();

        EntityUser user = UserUtils.addUser("test-user", "a@a.net", "123456", "Username :D", "Random BIO", new Date(0), "ES");
        EntitySong song = SongUtils.addSong(user, "Song 1", "ES");

        Assert.assertNotNull(user);
        Assert.assertNotNull(song);

        String s = new SongRequests().addListenedSong(user.getNick(), user.getToken().getToken(), song.getId());
        JSONObject obj = new JSONObject(s);

        Assert.assertTrue(UserCache.deleteUser(user));
        Assert.assertTrue(SongCache.deleteSong(song));

    }

}
