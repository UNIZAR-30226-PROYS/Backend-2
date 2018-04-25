package es.eina.requests;

import es.eina.cache.SongCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityAlbum;
import es.eina.utils.AlbumUtils;
import es.eina.utils.StringUtils;

import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/albums/")
@Produces(MediaType.APPLICATION_JSON)
public class AlbumRequests {

    private static final JSONObject defaultAlbumJSON;

    
    /**
     * Create a new album.
     *
     * @param userID : ID from user author of album.
     * @param title : Given name for the new album.
     * @param image : path to the image resource used as album cover.
     * @param year : year of the original album release.
     * @param songID : id of the first song of the album.
     * 
     * @return The result of this request.
     */
    @POST
    public String createAlbum(@FormParam("userID") long userID, @FormParam("title") String title,
    		@FormParam("image") String image, @FormParam("year") int year, 
    		@FormParam("songID") long songID){
    	
        JSONObject obj = new JSONObject();
        JSONObject albumJSON = new JSONObject(defaultAlbumJSON, JSONObject.getNames(defaultAlbumJSON));

        if(userID > 0){
        	if(StringUtils.isValid(title,1,255)) {
        		if(StringUtils.isValid(image)) {
        			if(year > 1900) {
			            EntitySong song = SongCache.getSong(songID);
			            if (song != null) {
			            	EntityAlbum album = AlbumUtils.createAlbum(userID, title,  year, image, song);
				            if(album != null){
				            	albumJSON.put("id", album.getAlbumId());
				            	albumJSON.put("user_id", album.getUserId());
				            	albumJSON.put("title", album.getTitle());
				            	albumJSON.put("publishYear", album.getPublishYear());
				            	albumJSON.put("upload_time", album.getUploadTime());
				            	albumJSON.put("image", album.getImage());
				            	albumJSON.put("songs", album.getSongStrings());
				                obj.put("error", "ok");
				            }else {
				            	obj.put("error", "AlbumCouldntBeCreated");
				            }
			            }else{
			                obj.put("error", "unknownSong");
			            }
        			}else {
        				obj.put("error", "invalidYear");
        			}
        		}else {
        			obj.put("error", "invalidImage");
        		}
        	}else {
        		obj.put("error", "invalidTitle");
        	}
        }else{
            obj.put("error", "invalidArgs");
        }

        obj.put("album", albumJSON);

        return obj.toString();
    }

    /**
     * Delete an album.
     *
     * @param albumID : ID from album to delete.
	 *
     * @return The result of this request.
     */
    @PATH("/{albumID}")
    @DELETE
    public String deleteAlbum(@PathParam("albumID") long albumID){
    	JSONObject obj = new JSONObject();
    	boolean OK;
	    if(albumID > 0) {
	    	OK = AlbumUtils.deleteAlbum(albumID);
	    	if (OK) {
	    		obj.put("error", "ok");
	    	}else {
	    		obj.put("error", "AlbumCouldntBeDeleted");
	    	}
    	}else{
	        obj.put("error", "invalidAlbumID");
    	}
    	return obj.toString();
    }
    
    static {
    	defaultAlbumJSON = new JSONObject();
    	defaultAlbumJSON.put("id", -1L);
    	defaultAlbumJSON.put("user_id", -1L);
    	defaultAlbumJSON.put("title", "");
    	defaultAlbumJSON.put("publish_year", -1);
    	defaultAlbumJSON.put("upload_time", -1L);
    	defaultAlbumJSON.put("image", "");
    }

}
