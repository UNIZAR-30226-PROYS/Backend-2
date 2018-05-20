package es.eina.requests;

import es.eina.cache.AlbumCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.AlbumUtils;
import es.eina.utils.StringUtils;

import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/albums/")
@Produces(MediaType.APPLICATION_JSON)
public class AlbumRequests {

    private static final JSONObject defaultAlbumJSON;


    /**
     * Create a new album.
     *
     * @param nick : ID from user author of album.
	 * @param userToken : User's token.
     * @param title : Given name for the new album.
     * @param image : path to the image resource used as album cover.
     * @param year : year of the original album release.
     *
     * @return The result of this request.
     */
    @Path("{nick}/create")
    @POST
    public String create(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
						 @FormParam("title") String title, @FormParam("image") String image,
						 @FormParam("year") int year){

        JSONObject obj = new JSONObject();
        JSONObject albumJSON = new JSONObject(defaultAlbumJSON, JSONObject.getNames(defaultAlbumJSON));

		if(StringUtils.isValid(nick) && StringUtils.isValid(userToken) && StringUtils.isValid(title, 1, 255)){
			EntityUser user = UserCache.getUser(nick);
			if(user != null){
				if (user.getToken() != null && user.getToken().isValid(userToken)) {
						if(StringUtils.isValid(image)) {
							if(year > 1900) {
								EntityAlbum album = AlbumUtils.createAlbum(user, title, year, image);
								if(album != null && AlbumCache.saveEntity(album)){
									albumJSON.put("id", album.getId());
									albumJSON.put("user_id", album.getUserId());
									albumJSON.put("title", album.getTitle());
									albumJSON.put("publish_year", album.getPublishYear());
									albumJSON.put("creation_time", album.getCreationTime());
									albumJSON.put("image", album.getImage());
									albumJSON.put("songs", album.getSongsAsArray());
									obj.put("error", "ok");
								}else {
									obj.put("error", "unknownError");
								}
							}else {
								obj.put("error", "invalidYear");
							}
						}else {
							obj.put("error", "invalidImage");
						}
				} else {
					obj.put("error", "invalidToken");
				}
			}else{
				obj.put("error", "unknownUser");
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
	 * @param nick : ID from user author of album.
	 * @param userToken : User's token.
	 * @param albumId : ID from the album.
	 *
	 * @return The result of this request.
     */
    @Path("/{albumID}/delete")
    @DELETE
    public String delete(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
						 @PathParam("albumID") long albumId){
    	JSONObject obj = new JSONObject();

		if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
			EntityUser user = UserCache.getUser(nick);
			if(user != null){
				if (user.getToken() != null && user.getToken().isValid(userToken)) {
					EntityAlbum album = AlbumCache.getAlbum(albumId);
					if (album != null) {
						AlbumCache.deleteAlbum(album);
						obj.put("error", "ok");
					}else{
						obj.put("error", "unknownAlbum");
					}
				} else {
					obj.put("error", "invalidToken");
				}
			}else{
				obj.put("error", "unknownUser");
			}
		}else{
			obj.put("error", "invalidArgs");
		}

		return obj.toString();

    }

	/**
	 * Add song to album.
	 *
	 * @param nick : ID from user author of album.
	 * @param userToken : User's token.
	 * @param albumId : ID from the album.
	 * @param songId : ID from the song to add
	 *
	 * @return The result of this request.
	 */
	@Path("/{nick}/{albumID}/add")
	@POST
	public String addSongToAlbum(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
						 @PathParam("albumID") long albumId, @FormParam("songId") long songId){
		JSONObject obj = new JSONObject();

		if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
			EntityUser user = UserCache.getUser(nick);
			if(user != null){
				if (user.getToken() != null && user.getToken().isValid(userToken)) {
					EntityAlbum album = AlbumCache.getAlbum(albumId);
					if (album != null) {
						EntitySong song = SongCache.getSong(songId);
						if(song != null){
							if(album.addSong(song)) {
								AlbumCache.updateAlbum(album);
								obj.put("error", "ok");
							}else{
								obj.put("error", "alreadyAdded");
							}
						}else {
							obj.put("error", "unknownSong");
						}
					}else{
						obj.put("error", "unknownAlbum");
					}
				} else {
					obj.put("error", "invalidToken");
				}
			}else{
				obj.put("error", "unknownUser");
			}
		}else{
			obj.put("error", "invalidArgs");
		}

		return obj.toString();

	}

	/**
	 * Delete song from album.
	 *
	 * @param nick : ID from user author of album.
	 * @param userToken : User's token.
	 * @param albumId : ID from the album.
	 * @param songId : ID from the song to add
	 *
	 * @return The result of this request.
	 */
	@Path("/{nick}/{albumID}/{songId}/delete")
	@DELETE
	public String removeSongFromAlbum(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
						 @PathParam("albumID") long albumId, @PathParam("songId") long songId){
		JSONObject obj = new JSONObject();

		if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
			EntityUser user = UserCache.getUser(nick);
			if(user != null){
				if (user.getToken() != null && user.getToken().isValid(userToken)) {
					EntityAlbum album = AlbumCache.getAlbum(albumId);
					if (album != null) {
						EntitySong song = SongCache.getSong(songId);
						if(song != null){
							if(album.removeSong(song)) {
								AlbumCache.updateAlbum(album);
								obj.put("error", "ok");
							}else{
								obj.put("error", "alreadyRemoved");
							}
						}else {
							obj.put("error", "unknownSong");
						}
					}else{
						obj.put("error", "unknownAlbum");
					}
				} else {
					obj.put("error", "invalidToken");
				}
			}else{
				obj.put("error", "unknownUser");
			}
		}else{
			obj.put("error", "invalidArgs");
		}

		return obj.toString();

	}

    static {
    	defaultAlbumJSON = new JSONObject();
    	defaultAlbumJSON.put("id", -1L);
    	defaultAlbumJSON.put("user_id", -1L);
    	defaultAlbumJSON.put("title", "");
    	defaultAlbumJSON.put("publish_year", -1);
    	defaultAlbumJSON.put("creation_time", -1L);
    	defaultAlbumJSON.put("image", "");
    }

}
