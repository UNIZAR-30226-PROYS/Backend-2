package es.eina.requests;

import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/songs/")
@Produces(MediaType.APPLICATION_JSON)
public class SongRequests {

    private static final JSONObject defaultSongJSON;

    @Path("/{id}/")
    @GET
    public String getSong(@PathParam("id") long id){
        JSONObject obj = new JSONObject();
        JSONObject songJSON = new JSONObject(defaultSongJSON, JSONObject.getNames(defaultSongJSON));

        if(id > 0){
            try(Session s = HibernateUtils.getSession()) {
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
        }else{
            obj.put("error", "invalidArgs");
        }

        obj.put("song", songJSON);

        return obj.toString();
    }


    /**
     * Obtains song's likes in the database.
     * @param id : Song's ID.
     * @return A JSON with response.
     */
    @Path("/{id}/likes")
    @GET
    public static String getLikes(@PathParam("id") long id){
        JSONObject result = new JSONObject();

        try(Session s = HibernateUtils.getSession()) {
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
            try(Session s = HibernateUtils.getSession()) {
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

                if(ok) {
                    t.commit();
                }else{
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
            try(Session s = HibernateUtils.getSession()) {
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
                if(ok) {
                    t.commit();
                }else{
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
            try(Session s = HibernateUtils.getSession()) {
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

                if(ok) {
                    t.commit();
                }else{
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
            try(Session s = HibernateUtils.getSession()) {
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

                if(ok) {
                    t.commit();
                }else{
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
     * @return A JSON with response.
     */
    @Path("user/{nick}/faved")
    @GET
    public String getFavedSongs(@PathParam("nick") String nick) {
        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick)) {
            try(Session s = HibernateUtils.getSession()) {
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

                if(ok) {
                    t.commit();
                }else{
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
