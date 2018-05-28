package es.eina;

import es.eina.sql.utils.HibernateUtils;
import es.eina.sql.utils.HibernateUtilsTest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.InputStream;

public class TestBase {

    protected Session s;
    protected Transaction t;

    protected void openSession(){
        s = HibernateUtils.getSession();
        t = s.beginTransaction();
    }

    protected void closeSession(){
        t.commit();
        s.close();
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
}
