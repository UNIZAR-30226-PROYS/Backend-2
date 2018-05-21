package es.eina.requests;

import es.eina.RestApp;
import es.eina.cache.SongCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.search.IndexProduct;
import es.eina.search.IndexSongs;
import es.eina.sql.entities.EntitySong;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/songs/")
@Produces(MediaType.APPLICATION_JSON)
public class SongRequests {

    private static final int SONG_SEARCH_NUMBER = 10;
    private static final long SONG_SEARCH_MIN_TIME = 0;
    private static final long SONG_SEARCH_MAX_TIME = Long.MAX_VALUE;

    private static final JSONObject defaultSongJSON;

    @Path("/{id}/")
    @GET
    public String getSong(@PathParam("id") long id){
        JSONObject obj = new JSONObject();
        JSONObject songJSON = new JSONObject(defaultSongJSON, JSONObject.getNames(defaultSongJSON));

        if(id > 0){
            EntitySong song = SongCache.getSong(id);
            if(song != null){
                songJSON.put("id", song.getId());
                songJSON.put("user_id", song.getUserId());
                songJSON.put("title", song.getTitle());
                songJSON.put("country", song.getCountry());
                songJSON.put("upload_time", song.getUploadTime());
                obj.put("error", "ok");
            }else{
                obj.put("error", "unknownSong");
            }
        }else{
            obj.put("error", "invalidArgs");
        }

        obj.put("song", songJSON);

        return obj.toString();
    }

    @Path("/{id}/recommend")
    @GET
    public String getSongRecommendation(@PathParam("id") long id, @QueryParam("n") @DefaultValue("16") int amount){
        if(id > 0) {
            return RestApp.getInstance().getRecommender().recommend(SongCache.getSong(id), amount).toString();
        }else{
            return "{\"error\": \"invalidArgs\"}";
        }
    }
    
    /**
     * Perform a search of products in the database.<br>
     *     <p>
     *         URI: /songs/search/?query=[&n={number}][&country={country}][&genre={genre}][&min_time={min_time}][&max_time={max_time}]
     *     </p>
     * @param number : Number of results to return
     * @param keywords : Keywords to search
     * @return The result of this search as specified in API.
     */
    @Path("/search")
    @GET
    public String searchProducts(
            @DefaultValue("" + SONG_SEARCH_NUMBER) @QueryParam("n") int number,
            @DefaultValue("") @QueryParam("query") String keywords,
            @DefaultValue("") @QueryParam("country") String country,
            @DefaultValue("") @QueryParam("genre") String genre,
            @DefaultValue("" + SONG_SEARCH_MIN_TIME) @QueryParam("min_time") long minTime,
            @DefaultValue("" + SONG_SEARCH_MAX_TIME) @QueryParam("max_time") long maxTime
    ){
        minTime = Math.max(SONG_SEARCH_MIN_TIME, minTime);

        JSONObject obj = new JSONObject();
        JSONObject searchParams = new JSONObject();
        JSONArray songs = new JSONArray();

        searchParams.put("query", keywords);
        searchParams.put("min_time", minTime);
        searchParams.put("max_time", maxTime);

        IndexSongs index = RestApp.getSongsIndex();
        index.setSearchParams(genre, country, minTime, maxTime);
        List<ScoreDoc> result = index.search(keywords, number);

        if(result != null) {
            for (ScoreDoc score : result) {
                Document doc = index.getDocument(score.doc);
                float luceneScore = score.score;

                JSONObject product = new JSONObject(defaultSongJSON, JSONObject.getNames(defaultSongJSON));
                product.put("id", doc.get(IndexSongs.ID_INDEX_COLUMN));
                product.put("title", doc.get(IndexSongs.TITLE_INDEX_COLUMN));
                product.put("user_id", doc.get(IndexSongs.AUTHOR_ID_INDEX_COLUMN));
                product.put("country", doc.get(IndexSongs.COUNTRY_INDEX_COLUMN));
                product.put("upload_time", doc.get(IndexSongs.UPLOAD_TIME_INDEX_COLUMN));
                product.put("score", luceneScore);

                songs.put(product);
            }
            obj.put("number", result.size());
        }else{
            obj.put("number", 0);
        }

        obj.put("params", searchParams);
        obj.put("songs", songs);
        return obj.toString();
    }

    static {
        defaultSongJSON = new JSONObject();
        defaultSongJSON.put("id", -1L);
        defaultSongJSON.put("user_id", -1L);
        defaultSongJSON.put("title", "");
        defaultSongJSON.put("country", Geolocalizer.DEFAULT_COUNTRY);
        defaultSongJSON.put("upload_time", -1L);
    }

}
