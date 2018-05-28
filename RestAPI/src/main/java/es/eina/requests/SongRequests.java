package es.eina.requests;

import es.eina.RestApp;
import es.eina.cache.PopularSongCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.search.IndexSongs;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
    public static final int MAX_POPULAR_SONGS = 50;

    private static final JSONObject defaultSongJSON;

    @Path("/{id}/")
    @GET
    public String getSong(@PathParam("id") long id) {
        JSONObject obj = new JSONObject();
        JSONObject songJSON = new JSONObject(defaultSongJSON, JSONObject.getNames(defaultSongJSON));

        if (id > 0) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                EntitySong song = SongCache.getSong(s, id);
                if (song != null) {
                    songJSON.put("id", song.getId());
                    songJSON.put("user_id", song.getUserId());
                    songJSON.put("title", song.getTitle());
                    songJSON.put("country", song.getCountry());
                    songJSON.put("upload_time", song.getUploadTime());
                    obj.put("error", "ok");
                    t.commit();
                } else {
                    obj.put("error", "unknownSong");
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }

        obj.put("song", songJSON);

        return obj.toString();
    }

    @Path("/{id}/recommend")
    @GET
    public String getSongRecommendation(@PathParam("id") long id, @QueryParam("n") @DefaultValue("16") int amount) {
        if (id > 0) {
            try(Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                JSONObject obj =  RestApp.getInstance().getRecommender().recommend(SongCache.getSong(s, id), amount);
                if("ok".equals(obj.get("error"))){
                    t.commit();
                }else{
                    t.rollback();
                }
                return obj.toString();
            }
        } else {
            return "{\"error\": \"invalidArgs\"}";
        }
    }

    /**
     * Perform a search of products in the database.<br>
     * <p>
     * URI: /songs/search/?query=[&n={number}][&country={country}][&genre={genre}][&min_time={min_time}][&max_time={max_time}]
     * </p>
     *
     * @param number   : Number of results to return
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
    ) {
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

        if (result != null) {
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
        } else {
            obj.put("number", 0);
        }

        obj.put("params", searchParams);
        obj.put("songs", songs);
        return obj.toString();
    }

    @Path("/popular/")
    @GET
    public String getPopularSongs(@QueryParam("n") @DefaultValue("" + MAX_POPULAR_SONGS) int amount) {
        amount = Math.max(0, Math.min(MAX_POPULAR_SONGS, amount));
        JSONObject obj;
        try(Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            obj = PopularSongCache.getPopularSongs(s, amount);
            if("ok".equals(obj.getString("error"))){
                t.commit();
            }else{
                t.rollback();
            }
        }
        return obj.toString();
    }

    @Path("/popular/{country}/")
    @GET
    public String getPopularSongs(@PathParam("country") String country, @QueryParam("n") @DefaultValue("" + MAX_POPULAR_SONGS) int amount) {
        amount = Math.max(0, Math.min(MAX_POPULAR_SONGS, amount));
        JSONObject obj;
        try(Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            obj = PopularSongCache.getPopularSongs(s, amount, country);
            if("ok".equals(obj.getString("error"))){
                t.commit();
            }else{
                t.rollback();
            }
        }
        return obj.toString();
    }

    @Path("/popular/user/{id}/")
    @GET
    public String getPopularSongs(@PathParam("id") int id, @QueryParam("n") @DefaultValue("" + MAX_POPULAR_SONGS) int amount) {
        amount = Math.max(1, Math.min(MAX_POPULAR_SONGS, amount));

        JSONObject obj = new JSONObject();
        try(Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            EntityUser user = UserCache.getUser(s, id);
            if (user != null) {
                obj = PopularSongCache.getPopularSongs(s, amount);
            }else{
                obj.put("error", "unknownUser");
            }
            if ("ok".equals(obj.getString("error"))) {
                t.commit();
            } else {
                t.rollback();
            }
        }
        return obj.toString();
    }


    /**
     * Obtains song's likes in the database.
     *
     * @param id : Song's ID.
     * @return A JSON with response.
     */
    @Path("/{id}/likes")
    @GET
    public static String getLikes(@PathParam("id") long id) {
        JSONObject result = new JSONObject();

        try (Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            EntitySong song = SongCache.getSong(s, id);
            if (song != null) {
                result.put("likes", SQLUtils.getRowCountSQL(s, "song_likes", "song_id = " + id));
                result.put("error", "ok");
                t.commit();
            } else {
                result.put("error", "unknownSong");
                t.rollback();
            }
        }

        return result.toString();
    }

    /**
     * Add a new song to user's list of listened songs
     *
     * @param nick      : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param songId:   Song's ID
     * @return The result of this query as specified in API.
     */
    @Path("{songId}/listen")
    @POST
    public String listenSong(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                             @PathParam("songId") long songId) {
        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick) && StringUtils.isValid(userToken)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(userToken)) {
                        EntitySong song = SongCache.getSong(s, songId);
                        if (song != null) {
                            if (user.listenSong(song)) {
                                result.put("error", "ok");
                                ok = true;
                            } else {
                                result.put("error", "unknownError");
                            }
                        } else {
                            result.put("error", "unknownSong");
                        }
                    } else {
                        result.put("error", "invalidToken");
                    }
                } else {
                    result.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }

    /**
     * Add a song to user's fav list.
     *
     * @param nick   : User's nick.
     * @param token  : User's token.
     * @param songID : Song's ID.
     * @return A JSON with response.
     */
    @Path("user/{nick}/fav")
    @POST
    public String favSong(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String token,
                          @FormParam("songId") Long songID) {
        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick) && StringUtils.isValid(token)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(token)) {
                        EntitySong song = SongCache.getSong(s, songID);
                        if (song != null) {
                            if (!song.isSongFaved(user) && !user.isSongFaved(song)) {
                                if (song.favSong(user) && user.favSong(song)) {
                                    result.put("error", "ok");
                                    ok = true;
                                } else {
                                    result.put("error", "unknownError");
                                }
                            } else {
                                result.put("error", "alreadyFav");
                            }
                        } else {
                            result.put("error", "unknownSong");
                        }
                    } else {
                        result.put("error", "invalidToken");
                    }
                } else {
                    result.put("error", "unknownUser");
                }
                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }

    /**
     * Remove a song from user's fav list.
     *
     * @param nick   : User's nick.
     * @param token  : User's token.
     * @param songID : Song's ID.
     * @return A JSON with response.
     */
    @Path("user/{nick}/unfav")
    @POST
    public String unfavSong(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String token,
                            @FormParam("songId") Long songID) {
        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick) && StringUtils.isValid(token)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(token)) {
                        EntitySong song = SongCache.getSong(s, songID);
                        if (song != null) {
                            if (song.isSongFaved(user) && user.isSongFaved(song)) {
                                if (song.unfavSong(user) && user.unfavSong(song)) {
                                    result.put("error", "ok");
                                    ok = true;
                                } else {
                                    result.put("error", "unknownError");
                                }
                            } else {
                                result.put("error", "noFav");
                            }
                        } else {
                            result.put("error", "unknownSong");
                        }
                    } else {
                        result.put("error", "invalidToken");
                    }
                } else {
                    result.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }

    /**
     * Remove a song from user's fav list.
     *
     * @param nick   : User's nick.
     * @param songID : Song's ID.
     * @return A JSON with response.
     */
    @Path("user/{nick}/faved/{songId}")
    @GET
    public String hasFaved(@PathParam("nick") String nick, @PathParam("songId") Long songID) {
        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    EntitySong song = SongCache.getSong(s, songID);
                    if (song != null) {
                        if (song.isSongFaved(user)) {
                            result.put("error", "ok");
                            ok = true;
                        } else {
                            result.put("error", "noFav");
                        }
                    } else {
                        result.put("error", "unknownSong");
                    }
                } else {
                    result.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }

    /**
     * Remove a song from user's fav list.
     *
     * @param nick : User's nick.
     * @return A JSON with response.
     */
    @Path("user/{nick}/faved")
    @GET
    public String getFavedSongs(@PathParam("nick") String nick) {
        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    JSONArray favedSongs = user.getFavedSongs();
                    result.put("songs", favedSongs);
                    result.put("size", favedSongs.length());
                    result.put("error", "ok");
                    ok = true;
                } else {
                    result.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            result.put("error", "invalidArgs");
        }

        return result.toString();
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
