package es.eina.requests;

import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.StringUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Objects;
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
     * URI: /user-lists/{nick}/create
     * </p>
     *
     * @param nick  : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param title : List title
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/create")
    @POST
    public String create(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                         @FormParam("title") String title) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken) && StringUtils.isValid(title)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {

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
    @POST
    public String getLists(@PathParam("nick") String nick) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                List<EntitySongList> songlists = SongListCache.getSongLists(nick);
                result.put("size", songlists.size());
                for (EntitySongList song: songlists
                     ) {
                    result.put(Objects.toString(song.getId()),song);
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
     * URI: /user-lists/{nick}/{list}/delete
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param  listId: List ID
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/delete")
    @POST
    public String delete(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                         @PathParam("listId") long listId) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    SongListCache.deleteSonList(listId);
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
     * URI: /user-lists/{nick}/{list}/delete
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param listId: Id de la lista
     * @param songsId: Lista de ids de las canciones a añadir
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/add")
    @POST
    public String delete(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                         @PathParam("listId") long listId, @FormParam("songId") List<Long> songsId) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(userToken)) {
                    long authorId = UserCache.getId(nick);
                    SongListCache.addSongs(listId, songsId, authorId);
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
