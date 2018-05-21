package es.eina.requests;

import es.eina.cache.PopularSongCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/songs/")
@Produces(MediaType.APPLICATION_JSON)
public class SongRequests {

    public static final int MAX_POPULAR_SONGS = 50;

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

    @Path("/popular/")
    @GET
    public String getPopularSongs(@QueryParam("n") @DefaultValue("" + MAX_POPULAR_SONGS) int amount){
        amount = Math.max(0, Math.min(MAX_POPULAR_SONGS, amount));
        return PopularSongCache.getPopularSongs(amount).toString();
    }

    @Path("/popular/{country}/")
    @GET
    public String getPopularSongs(@PathParam("country") String country, @QueryParam("n") @DefaultValue("" + MAX_POPULAR_SONGS) int amount){
        amount = Math.max(0, Math.min(MAX_POPULAR_SONGS, amount));
        return PopularSongCache.getPopularSongs(amount, country).toString();
    }

    @Path("/popular/user/{id}/")
    @GET
    public String getPopularSongs(@PathParam("id") int id, @QueryParam("n") @DefaultValue("" + MAX_POPULAR_SONGS) int amount){
        amount = Math.max(1, Math.min(MAX_POPULAR_SONGS, amount));

        EntityUser user = UserCache.getUser(id);
        if(user != null) {
            return PopularSongCache.getPopularSongs(amount, user.getCountry()).toString();
        }else{
            JSONObject obj = new JSONObject();
            obj.put("error", "unknownUser");
            return obj.toString();
        }
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
