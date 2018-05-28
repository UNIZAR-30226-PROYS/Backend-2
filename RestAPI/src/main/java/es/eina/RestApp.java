package es.eina;


import es.eina.recommender.Recommender;
import es.eina.search.IndexSongs;
import es.eina.search.IndexUsers;

import java.io.File;
import java.util.logging.Logger;

public class RestApp {

    private static RestApp instance;
    private final Logger logger = Logger.getLogger("webLogger");
    private static final IndexSongs songsIndex = new IndexSongs();
    private static final IndexUsers usersIndex = new IndexUsers();

    private final Recommender recommender;

    public RestApp() {
        instance = this;
        recommender = new Recommender();
        //index.openIndex("index/");
        new File("indices/songIndex/").mkdirs();
        new File("indices/userIndex/").mkdirs();
        songsIndex.openIndex("indices/songIndex/");
        usersIndex.openIndex("indices/userIndex/");

    }

    public static RestApp getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public Recommender getRecommender() {
        return recommender;
    }

    public static IndexSongs getSongsIndex() {
        return songsIndex;
    }

    public static IndexUsers getUsersIndex() {
        return usersIndex;
    }

    public void close(){
        songsIndex.close();
        usersIndex.close();
    }
}
