package es.eina.requests;

import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Path("/user-lists/")
@Produces(MediaType.APPLICATION_JSON)
public class UserSongListRequests {

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    /**
     * Create a new list
     * <p>
     * URI: /user-lists/{nick}/create/{title}
     * {token:"token"}
     * </p>
     *
     * @param nick  : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param title : List title
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/create/")
    @PUT
    public String create(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                         @FormParam("title") String title) {
        JSONObject result = new JSONObject();

        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken) && StringUtils.isValid(title)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    EntitySongList newSong= new EntitySongList(title, user);
                    if(SongListCache.saveEntity(newSong)){
                        result.put("error","ok");
                    }else{
                        result.put("error","unexpectedError");
                    }
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }
    /**
     * Get all list from a user
     * <p>
     * URI: /user-lists/{nick}/lists
     * {token:"token"}
     * </p>
     *
     * @param nick  : Nickname of a user to create the list
     * @return Si no hay error devuelve todas las ids de las playlists del usuario.
     */
    @Path("{nick}")
    @GET
    public String getLists(@PathParam("nick") String nick) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                List<EntitySongList> songlists = SongListCache.getSongLists(nick);
                result.put("size", songlists.size());
                JSONArray jsonarray = new JSONArray();
                for (EntitySongList song: songlists
                     ) {
                    jsonarray.put(song.getId());
                }
                result.put("id", jsonarray);
                result.put("error", "ok");
            }else{
                result.put("error", "unknownUser");
            }

        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }

    /**
     * Delete a list
     * <p>
     * URI: /user-lists/{nick}/delete
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param  listId: List ID
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/delete")
    @DELETE
    public String delete(@PathParam("nick") String nick,
                         @DefaultValue("") @QueryParam("token") String userToken,
                         @QueryParam("listid") long listId) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    SongListCache.deleteSongList(listId);
                    result.put("error", "ok");
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }
    /**
     * add songs to list
     * <p>
     * URI: /user-lists/{nick}/{list}/add
     * {token:"token"
     * songsid:[1,2,3]}
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @param listId: Id de la lista
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String add(@PathParam("nick") String nick,
                         @PathParam("listId") Long listId,@DefaultValue("") String Body) {
        JSONObject json = new JSONObject(Body);
        String userToken = (String) json.get("token");
        JSONArray jsonlist = (JSONArray) json.get("songsIds");
        List<Long> songsId = new ArrayList<>();

        for (Object item:jsonlist
                ) {
            Integer oneitem = (Integer) item;
            songsId.add(oneitem.longValue());
        }
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    long authorId = UserCache.getId(nick);
                    int error = SongListCache.addSongs(listId, songsId, authorId);
                    switch (error) {
                        case 0 : result.put("error", "ok");
                            break;
                        case 1: result.put("error", "invalidSongList");
                            break;
                        case 2: result.put("error", "invalidAuthor");
                            break;
                        default: result.put("error", "unexpectedError");
                            break;
                    }
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }
    /**
     * Delete a list
     * <p>
     * URI: /user-lists/{nick}/{list}/remove
     * {token:"token"
     * songsid:[1,2,3]}
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @param listId: Id de la lista
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String remove(@PathParam("nick") String nick, @DefaultValue("") String Body,
                      @PathParam("listId") Long listId) {

        JSONObject json = new JSONObject(Body);
        String userToken = (String) json.get("token");
        JSONArray jsonlist = (JSONArray) json.get("songsIds");
        List<Long> songsId = new ArrayList<>();

        for (Object item:jsonlist
                ) {
            Integer oneitem = (Integer) item;
            songsId.add(oneitem.longValue());
        }
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    long authorId = UserCache.getId(nick);
                    int error = SongListCache.removeSongs(listId, songsId, authorId);
                    switch (error) {
                        case 0 : result.put("error", "ok");
                            break;
                        case 1: result.put("error", "invalidSongList");
                            break;
                        case 2: result.put("error", "invalidAuthor");
                            break;
                        default: result.put("error", "unexpectedError");
                            break;
                    }
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }

}
