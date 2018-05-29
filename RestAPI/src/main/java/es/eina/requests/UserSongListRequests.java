package es.eina.requests;

import es.eina.cache.SongCache;
import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
            try(Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(userToken)) {
                        EntitySongList newSong = new EntitySongList(title, user);
                        if (SongListCache.addSongList(s, newSong)) {
                            result.put("error", "ok");
                            ok = true;
                        } else {
                            result.put("error", "unexpectedError");
                        }
                    } else {
                        result.put("error", "invalidToken");
                    }
                } else {
                    result.put("error", "unknownUser");
                }

                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
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
        JSONArray array = new JSONArray();
        if(StringUtils.isValid(nick)){
            try(Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    List<EntitySongList> songlists = SongListCache.getSongLists(s, nick);
                    result.put("size", songlists.size());
                    for (EntitySongList song : songlists) {
                        array.put(song.getId());
                    }
                    result.put("error", "ok");
                    ok = true;
                } else {
                    result.put("error", "unknownUser");
                }
                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
            }
        }else{
            result.put("error", "invalidArgs");
        }

        result.put("songs", array);

        return result.toString();
    }

    /**
     * Get a list
     * <p>
     * URI: /user-lists/{nick}/lists
     * </p>
     *
     * @param nick  : Nickname of a user to create the list
     * @return Si no hay error devuelve todas las ids de las playlists del usuario.
     */
    @Path("{id}")
    @GET
    public String getList(@PathParam("id") int id) {
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        if(id > 0){
            try(Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntitySongList list = SongListCache.getSongList(s, id);
                if(list != null){
                    result.put("list", list.toJSON());
                    result.put("error", "ok");
                    ok = true;
                } else {
                    result.put("error", "unknownList");
                }
                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
            }
        }else{
            result.put("error", "invalidArgs");
        }

        result.put("songs", array);

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
            try(Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(userToken)) {
                        EntitySongList songList = SongListCache.getSongList(s, listId);
                        if(songList != null) {
                            SongListCache.deleteSongList(s, songList);
                            result.put("error", "ok");
                            ok = true;
                        }else{
                            result.put("error", "unknownList");
                        }
                    } else {
                        result.put("error", "invalidToken");
                    }
                } else {
                    result.put("error", "unknownUser");
                }
                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }
    /**
     * Delete a list
     * <p>
     * URI: /user-lists/{nick}/{list}/add
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
    public String add(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                         @PathParam("listId") long listId, @FormParam("songId") Long songsId) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            try(Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    EntitySong song = SongCache.getSong(s, songsId);
                    if (song != null) {
                        if (user.getToken() != null && user.getToken().isValid(userToken)) {
                            EntitySongList list = SongListCache.getSongList(s, listId);
                            if (list != null) {
                                if(list.getAuthor().getId().equals(user.getId())) {
                                    list.addSong(song);
                                    result.put("error", "ok");
                                    ok = true;
                                } else {
                                    result.put("error", "notAuthor");
                                }
                            } else {
                                result.put("error", "unknownList");
                            }
                        } else {
                            result.put("error", "invalidToken");
                        }
                    } else {
                        result.put("error", "unknownSong");
                    }
                } else {
                    result.put("error", "unknownUser");
                }

                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }
    /**
     * Delete a list
     * <p>
     * URI: /user-lists/{nick}/{list}/add
     * </p>
     *
     *
     * @param nick  : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param listId: Id de la lista
     * @param songsId: Lista de ids de las canciones a añadir
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/remove")
    @POST
    public String remove(@FormParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
                      @PathParam("listId") long listId, @FormParam("songId") Long songsId) {
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(userToken)){
            try(Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    EntitySong song = SongCache.getSong(s, songsId);
                    if (song != null) {
                        if (user.getToken() != null && user.getToken().isValid(userToken)) {
                            EntitySongList list = SongListCache.getSongList(s, listId);
                            if (list != null) {
                                if(user.getId().equals(list.getAuthor().getId())) {
                                    list.removeSong(song);
                                    ok = true;
                                    result.put("error", "ok");
                                }else{
                                    result.put("error", "notAuthor");
                                }
                            }else{
                                result.put("error", "unknownList");
                            }
                        }else{
                            result.put("error", "invalidToken");
                        }
                    } else {
                        result.put("error", "unknownSong");
                    }
                } else {
                    result.put("error", "unknownUser");
                }

                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }



}
