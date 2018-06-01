package es.eina.requests;

import es.eina.RestApp;
import es.eina.cache.AlbumCache;
import es.eina.cache.PopularSongCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.search.IndexSongs;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.SongUtils;
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

    /**
     * Create a new album.
     *
     * @param nick      : ID from user author of album.
     * @param userToken : User's token.
     * @param title     : Given name for the new album.
     * @return The result of this request.
     */
    @Path("{nick}/create")
    @POST
    public String create(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                         @FormParam("title") String title, @DefaultValue("-1") @FormParam("albumID") long albumID,
                         @FormParam("country") String country, @DefaultValue("") @FormParam("genre") String genre) {

        JSONObject obj = new JSONObject();
        JSONObject songJSON = new JSONObject(EntitySong.defaultSongJSON);

        if (StringUtils.isValid(nick) && StringUtils.isValid(userToken) &&
                StringUtils.isValid(title, 1, 255)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(userToken)) {
                        EntityAlbum album = AlbumCache.getAlbum(s, albumID);
                        if (albumID == -1 || album != null) {
                            EntitySong song = SongUtils.addSong(s, album, title, country, genre);
                            if (song != null) {
                                songJSON = song.toJSON();
                                ok = true;
                                obj.put("error", "ok");
                            } else {
                                obj.put("error", "unknownError");
                            }
                        } else {
                            obj.put("error", "invalidAlbum");
                        }
                    } else {
                        obj.put("error", "invalidToken");
                    }
                } else {
                    obj.put("error", "unknownUser");
                }
                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }

        obj.put("song", songJSON);

        return obj.toString();
    }

    /**
     * Delete an album.
     *
     * @param nick      : ID from user author of album.
     * @param userToken : User's token.
     * @param songId    : ID of the song.
     * @return The result of this request.
     */
    @Path("/{songID}/delete")
    @POST
    public String delete(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                         @PathParam("songID") long songId) {
        JSONObject obj = new JSONObject();

        if (StringUtils.isValid(nick) && StringUtils.isValid(userToken)) {
            try (Session s = HibernateUtils.getSession()) {
                boolean ok = false;
                Transaction t = s.beginTransaction();
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(userToken)) {
                        if (songId > 0) {
                            EntitySong song = SongCache.getSong(s, songId);
                            if (song != null) {
                                if (SongCache.deleteSong(s, song)) {
                                    obj.put("error", "ok");
                                    ok = true;
                                } else {
                                    obj.put("error", "unknownSong");
                                }
                            } else {
                                obj.put("error", "unknownSong");
                            }
                        } else {
                            obj.put("error", "invalidSong");
                        }
                    } else {
                        obj.put("error", "invalidToken");
                    }
                } else {
                    obj.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }

        return obj.toString();

    }

    @Path("/{id}/")
    @GET
    public String getSong(@PathParam("id") long id) {
        JSONObject obj = new JSONObject();
        JSONObject songJSON = new JSONObject(EntitySong.defaultSongJSON);

        if (id > 0) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                EntitySong song = SongCache.getSong(s, id);
                if (song != null) {
                    songJSON = song.toJSON();
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
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                JSONObject obj = RestApp.getInstance().getRecommender().recommend(SongCache.getSong(s, id), amount);
                if ("ok".equals(obj.get("error"))) {
                    t.commit();
                } else {
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
    public String searchSongs(
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

                JSONObject song = new JSONObject(EntitySong.defaultSongJSON);
                song.put("id", doc.get(IndexSongs.ID_INDEX_COLUMN));
                song.put("title", doc.get(IndexSongs.TITLE_INDEX_COLUMN));
                song.put("user_id", doc.get(IndexSongs.AUTHOR_ID_INDEX_COLUMN));
                song.put("country", doc.get(IndexSongs.COUNTRY_INDEX_COLUMN));
                song.put("upload_time", doc.get(IndexSongs.UPLOAD_TIME_INDEX_COLUMN));
                song.put("score", luceneScore);

                songs.put(song);
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
    public String getPopularSongs(@QueryParam("n") @DefaultValue("" + MAX_POPULAR_SONGS) int amount,
                                @QueryParam("daily") @DefaultValue("false") boolean daily) {
        amount = Math.max(0, Math.min(MAX_POPULAR_SONGS, amount));
        long initTime = System.currentTimeMillis() - (daily ? 86400000L : 604800000L) ;
        JSONObject obj;
        try (Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            obj = PopularSongCache.getPopularSongs(s, amount, initTime);
            if ("ok".equals(obj.getString("error"))) {
                t.commit();
            } else {
                t.rollback();
            }
        }
        return obj.toString();
    }

    @Path("/popular/{country}/")
    @GET
    public String getPopularSongs(@PathParam("country") String country, @QueryParam("n") @DefaultValue("" + MAX_POPULAR_SONGS) int amount,
                                  @QueryParam("daily") @DefaultValue("false") boolean daily) {
        amount = Math.max(0, Math.min(MAX_POPULAR_SONGS, amount));
        long initTime = System.currentTimeMillis() - (daily ? 86400000L : 604800000L) ;
        JSONObject obj;
        try (Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            obj = PopularSongCache.getPopularSongs(s, amount, country, initTime);
            if ("ok".equals(obj.getString("error"))) {
                t.commit();
            } else {
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
        try (Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            EntityUser user = UserCache.getUser(s, id);
            if (user != null) {
                obj = PopularSongCache.getPopularSongs(s, amount);
            } else {
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
    @Path("/{id}/favs")
    @GET
    public String getFavs(@PathParam("id") long id) {
        JSONObject result = new JSONObject();

        try (Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            EntitySong song = SongCache.getSong(s, id);
            if (song != null) {
                result.put("favs", SQLUtils.getRowCountSQL(s, "user_faved_songs", "song_id = " + id));
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

    @Path("genres")
    @GET
    public String getGenres(){
        JSONObject object = new JSONObject();
        JSONArray genres = new JSONArray();

        for(EntitySong.EnumSongGenre genre : EntitySong.EnumSongGenre.values()){
            genres.put(genre.toString());
        }

        object.put("genres", genres);
        return object.toString();
    }

}
