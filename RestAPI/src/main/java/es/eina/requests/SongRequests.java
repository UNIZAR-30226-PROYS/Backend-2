package es.eina.requests;

import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.StringUtils;
import org.json.JSONObject;

import javax.transaction.Transactional;
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


    /**
     * Obtains song's likes in the database.
     * @param id : Song's ID.
     * @return A JSON with response.
     */
    @Path("/{id}/likes")
    @GET
    public static String getLikes(@PathParam("id") long id){
        JSONObject result = new JSONObject();
        EntitySong song = SongCache.getSong(id);
        if(song != null) {
            result.put("likes", SQLUtils.getRowCountSQL("song_likes", "song_id = " + id));
            result.put("error", "ok");
        }else{
            result.put("error", "unknownSong");
        }

        return result.toString();
    }


    @Path("/{id}/lyrics")
    @GET
    public String getSongLyrics(@PathParam("id") long id){
        JSONObject obj = new JSONObject();
        JSONObject songJSON = new JSONObject(defaultSongJSON, JSONObject.getNames(defaultSongJSON));

        if(id > 0){
            EntitySong song = SongCache.getSong(id);
            if(song != null){
                songJSON.put("id", song.getId());
                songJSON.put("lyrics", song.getLyrics());

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

    /**
     * Add a new song to user's list of listened songs
     *
     * @param nick      : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param songId:   Song's ID
     * @return The result of this query as specified in API.
     */
    @Path("{songId}/listen")
    @Transactional
    @POST
    public JSONObject addListenedSong(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                                  @PathParam("songId") long songId) {
        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick) && StringUtils.isValid(userToken)) {
            EntityUser user = UserCache.getUser(nick);
            if (user != null) {
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    EntitySong song = SongCache.getSong(songId);
                    if (song != null) {
                        if (user.listenSong(song)) {
                            result.put("error", "ok");
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
        } else {
            result.put("error", "invalidArgs");
        }

        return result;

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
