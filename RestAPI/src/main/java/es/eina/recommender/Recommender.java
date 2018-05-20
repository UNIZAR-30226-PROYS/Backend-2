package es.eina.recommender;

import es.eina.RestApp;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.utils.HibernateUtils;
import es.eina.task.TaskBase;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.*;

public class Recommender extends TaskBase {
    private ArrayList<Attribute> attributeList = new ArrayList<>();

    private static final String SONG_ID_COLUMN = "id";
    private static final String AUTHOR_ID_COLUMN = "author";
    private static final String COUNTRY_COLUMN = "country";

    private final Attribute songIdAttr;
    private final Attribute authorAttr;
    private final Attribute countryAttr;

    private Map<Long, Integer> songIds;
    private Map<Integer, List<Integer>> assignments;

    public Recommender(){
        super(1000,900000);
        songIdAttr = new Attribute(SONG_ID_COLUMN);
        authorAttr = new Attribute(AUTHOR_ID_COLUMN);
        countryAttr = new Attribute(COUNTRY_COLUMN, true);

        attributeList.add(songIdAttr);
        attributeList.add(authorAttr);
        attributeList.add(countryAttr);
        
    }

    @Override
    public void run() {
        SimpleKMeans kMeans = new SimpleKMeans();
        songIds = new TreeMap<>();
        Instances instances = new Instances("name", attributeList, 10);
        int amount = 0;
        try(Session s = HibernateUtils.getSessionFactory().openSession()) {
            Query<EntitySong> q = s.createQuery("FROM song", EntitySong.class);

            List<EntitySong> list = q.getResultList();
            amount = list.size();
            int i = 0;
            for(EntitySong song : list) {
                DenseInstance instance = new DenseInstance(10);
                songIds.put(song.getId(), i++);
                instance.setValue(songIdAttr, song.getId());
                instance.setValue(countryAttr, song.getCountry());
                instance.setValue(authorAttr, song.getUserId());

                instances.add(instance);
                RestApp.getInstance().getLogger().info("Adding " + song.getId());
            }
        }

        kMeans.setPreserveInstancesOrder(true);

        try {
            kMeans.setMaxIterations(500);
            kMeans.setNumClusters(Math.min(10, amount));
            kMeans.buildClusterer(instances);
            assignments = new HashMap<>();
            int[] temp = kMeans.getAssignments();
            for (int i = 0; i < temp.length; i++){
                List<Integer> l = assignments.getOrDefault(temp[i], new ArrayList<>());
                l.add(i);
            }
        } catch (Exception e) {
            RestApp.getInstance().getLogger().severe("Cannot cluster songs from Database.");
            RestApp.getInstance().getLogger().severe(e.getMessage());
        }

        RestApp.getInstance().getLogger().info("Finished building recommender system");
    }

    public JSONObject recommend(EntitySong base, int amount){
        JSONObject object = new JSONObject();
        JSONArray songs = new JSONArray();
        Integer cluster = songIds.get(base.getId());

        List<Integer> clusterAssignments = assignments.getOrDefault(cluster, new ArrayList<>());
        for(int i = 0; i < Math.min(clusterAssignments.size(), amount); i++){
            songs.put(clusterAssignments.get(i));
        }

        object.put("songs", songs);
        object.put("amount", songs.length());
        return object;
    }
}
