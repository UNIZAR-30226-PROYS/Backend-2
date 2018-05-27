package es.eina.requests;


import es.eina.cache.AlbumCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntitySong;
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

    /**
     * Create a new album.
     *
     * @param nick : ID from user author of album.
	 * @param userToken : User's token.
     * @param title : Given name for the new album.
     * @param year : year of the original album release.
     *
     * @return The result of this request.
     */
    @Path("{nick}/create")
    @POST
    public JSONObject create(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
						 @FormParam("title") String title, @FormParam("year") int year){

        JSONObject obj = new JSONObject();
        JSONObject albumJSON = EntityAlbum.defaultAlbumJSON;

		if(StringUtils.isValid(nick) && StringUtils.isValid(userToken) && StringUtils.isValid(title, 1, 255)){
			EntityUser user = UserCache.getUser(nick);
			if(user != null){
				if (user.getToken() != null && user.getToken().isValid(userToken)) {
							if(year > 1900) {
								EntityAlbum album = AlbumUtils.createAlbum(user, title, year);
								if(album != null && AlbumCache.addAlbum(album)){
									albumJSON = album.toJSON();
									obj.put("error", "ok");
								}else {
									obj.put("error", "unknownError");
								}
							}else {
								obj.put("error", "invalidYear");
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

        return obj;
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
    @POST
    public JSONObject delete(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
						 @PathParam("albumID") long albumId){
    	JSONObject obj = new JSONObject();

		if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
			EntityUser user = UserCache.getUser(nick);
			if(user != null){
				if (user.getToken() != null && user.getToken().isValid(userToken)) {
					if(albumId > 0) {
						EntityAlbum album = AlbumCache.getAlbum(albumId);
						if (album != null) {
							obj.put("error", AlbumCache.deleteAlbum(album) ? "ok" : "unknownAlbum");
						} else {
							obj.put("error", "unknownAlbum");
						}
					} else {
                        obj.put("error", "invalidAlbum");
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

		return obj;

    }

	/**
	 * Get an album.
	 *
	 * @return The result of this request.
	 */
	@Path("{id}")
	@GET
	public JSONObject get(@PathParam("id") long id){

		JSONObject obj = new JSONObject();
		JSONObject albumJSON = EntityAlbum.defaultAlbumJSON;
		if(id > 0){
			EntityAlbum album = AlbumCache.getAlbum(id);
			if(album != null){
				albumJSON = album.toJSON();
				obj.put("error", "ok");
			}else{
				obj.put("error", "unknownAlbum");
			}
		}else{
			obj.put("error", "invalidArgs");
		}

		obj.put("album", albumJSON);

		return obj;
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
	public JSONObject addSongToAlbum(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
						 @PathParam("albumID") long albumId, @FormParam("songId") long songId){
		JSONObject obj = new JSONObject();

		if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
			EntityUser user = UserCache.getUser(nick);
			if(user != null){
				if (user.getToken() != null && user.getToken().isValid(userToken)) {
				    if(albumId > 0) {
                        EntityAlbum album = AlbumCache.getAlbum(albumId);
                        if (album != null) {
                                if (songId > 0) {
                                    EntitySong song = SongCache.getSong(songId);
                                    if (song != null) {
                                        if(album.getUserId() == user.getId() && (song.getUserId() < 0 || album.getUserId() == song.getUserId())) {
                                            if (song.setAlbum(album)) {
                                                obj.put("error", "ok");
                                            } else {
                                                obj.put("error", "alreadyAdded");
                                            }
                                        }else{
                                            obj.put("error", "notAuthor");
                                        }
                                    } else {
                                        obj.put("error", "unknownSong");
                                    }

                                } else {
                                    obj.put("error", "invalidSong");
                                }

                        } else {
                            obj.put("error", "unknownAlbum");
                        }
                    }else{
				        obj.put("error", "invalidAlbum");
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

		return obj;

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
	@Path("/{nick}/{albumID}/delete")
	@POST
	public JSONObject removeSongFromAlbum(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
						 @PathParam("albumID") long albumId, @FormParam("songId") long songId) {
        JSONObject obj = new JSONObject();

        if (StringUtils.isValid(nick) && StringUtils.isValid(userToken)) {
            EntityUser user = UserCache.getUser(nick);
            if (user != null) {
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    if (albumId > 0) {
                        EntityAlbum album = AlbumCache.getAlbum(albumId);
                        if (album != null) {
                            if (album.getUserId() == user.getId()) {
                                if (songId > 0) {
                                    EntitySong song = SongCache.getSong(songId);
                                    if (song != null) {
                                        if (song.removeFromAlbum()) {
                                            obj.put("error", "ok");
                                        } else {
                                            obj.put("error", "alreadyRemoved");
                                        }
                                    } else {
                                        obj.put("error", "unknownSong");
                                    }
                                } else {
                                    obj.put("error", "invalidSong");
                                }
                            } else {
                                obj.put("error", "notAuthor");
                            }
                        } else {
                            obj.put("error", "unknownAlbum");
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
        } else {
            obj.put("error", "invalidArgs");
        }

        return obj;

    }

}
