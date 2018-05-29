package es.eina.requests;

import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntitySong;
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
import java.util.Set;

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
     * URI: /user-lists/{nick}/create/
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
                    if(SongListCache.addSongList(newSong)){
                        result.put("id", newSong.getId());
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
     * </p>
     *
     * @param nick  : Nickname of a user to create the list
     * @return Si no hay error devuelve todas las ids de las playlists del usuario.
     */
    @Path("{nick}/lists")
    @GET
    public String getLists(@PathParam("nick") String nick) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                Set<EntitySongList> songlists = SongListCache.getSongLists(nick);
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
     * Get all list info
     * <p>
     * URI: /user-lists/{list}
     * </p>
     *
     * @return Si no hay error devuelve todas la info de la playlist{list}.
     */
    @Path("{list}")
    @GET
    public String getLists(@PathParam("list") Long listID) {
        JSONObject result = new JSONObject();
        EntitySongList songlist = SongListCache.getSongList(listID);
        if (songlist != null){
                result.put("title", songlist.getTitle());
                result.put("author", songlist.getUserId());
                result.put("creation_time", songlist.getCreationTime());
                result.put("songs_size", songlist.getSongs().size());
                JSONArray jsonarray = new JSONArray();
                for (EntitySong song: songlist.getSongs()
                        ) {
                    jsonarray.put(song.getId());
                }
                result.put("songs", jsonarray);
            result.put("followers_size", songlist.getSongs().size());
            jsonarray = new JSONArray();
            for (EntityUser user: songlist.getFollowed()
                    ) {
                jsonarray.put(user.getId());
            }
            result.put("followers", jsonarray);
            result.put("error", "ok");
            }else{
                result.put("error", "unknownList");
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
                    EntitySongList songlist = SongListCache.getSongList(listId);
                    if (songlist.getUserId() == user.getId()){
                        if(SongListCache.deleteSongList(songlist)){
                            result.put("error", "ok");
                        }else{
                            result.put("error", "unExpectedError");
                        }
                    }else{
                        result.put("error", "unAuthorized");
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

    /**
     * Delete a list
     * <p>
     * URI: /user-lists/{nick}/{listId}/follow
     * {token:"token"
     * songsid:[1,2,3]}
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @param listId: Id de la lista
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/follow")
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    public String addfollower(@PathParam("nick") String nick, @FormParam("token") String userToken,
                         @PathParam("listId") Long listId) {

        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    int error = SongListCache.addFollower(listId, user);
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
                    }
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        return result.toString();

    }
    /**
     * Delete a list
     * <p>
     * URI: /user-lists/{nick}/{listId}/unfollow
     * token
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @param listId: Id de la lista
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/unfollow")
    @Consumes(MediaType.APPLICATION_JSON)
    @DELETE
    public String removefollower(@PathParam("nick") String nick, @QueryParam("token") String userToken,
                              @PathParam("listId") Long listId) {


        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    int error = SongListCache.removeFollower(listId, user);
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
                }
            } else {
                result.put("error", "invalidToken");
            }
        }else{
            result.put("error", "unknownUser");
        }
        return result.toString();

    }
    /**
     * Delete a list
     * <p>
     * URI: /user-lists/{nick}/following
     * token
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/following")
    @Consumes(MediaType.APPLICATION_JSON)
    @GET
    public String following (@PathParam("nick") String nick, @QueryParam("token") String userToken) {


        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    EntityUser oneuser = UserCache.getUser(nick);
                    Set<EntitySongList> following = user.getFollowing();
                    JSONArray jsonarray = new JSONArray();
                    for (EntitySongList songlist: following
                         ) {
                        jsonarray.put(songlist.getId());
                    }
                    result.put("id", jsonarray);
                    result.put("error", "ok");
                }
            } else {
                result.put("error", "invalidToken");
            }
        }else{
            result.put("error", "unknownUser");
        }
        return result.toString();

    }

}
