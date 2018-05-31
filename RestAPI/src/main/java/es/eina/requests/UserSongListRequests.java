package es.eina.requests;

import es.eina.cache.SongCache;
import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
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
     * @param nick      : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param title     : List title
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/create/")
    @PUT
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
                            result.put("list", newSong.toJSON());
                            result.put("error", "ok");
                            ok = true;
                        } else {
                            result.put("error", "unknownError");
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
        } else {
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
     * @param nick : Nickname of a user to create the list
     * @return Si no hay error devuelve todas las ids de las playlists del usuario.
     */
    @Path("{nick}/lists")
    @GET
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

        result.put("lists", array);
        return result.toString();
    }

    /**
     * Get a list
     * <p>
     * URI: /user-lists/{nick}/lists
     * </p>
     *
     * @return Si no hay error devuelve todas las ids de las playlists del usuario.
     *
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
    }*/


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
        if(listID > 0) {
            try (Session s = HibernateUtils.getSession()) {
                boolean ok = false;
                Transaction t = s.beginTransaction();
                EntitySongList songlist = SongListCache.getSongList(s, listID);
                if (songlist != null) {
                    JSONObject object = new JSONObject();
                    object.put("id", songlist.getId());
                    object.put("title", songlist.getTitle());
                    object.put("author", songlist.getAuthor().getId());
                    object.put("creation_time", songlist.getCreationTime());
                    object.put("songs_size", songlist.getSongs().size());
                    JSONArray jsonarray = new JSONArray();
                    for (EntitySong song : songlist.getSongs()) {
                        jsonarray.put(song.getId());
                    }
                    object.put("songs", jsonarray);
                    object.put("followers_size", songlist.getSongs().size());
                    jsonarray = new JSONArray();
                    for (EntityUser user : songlist.getFollowed()) {
                        jsonarray.put(user.getId());
                    }
                    object.put("followers", jsonarray);
                    result.put("list", object);
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
        } else {
            result.put("error", "invalidList");
        }

        return result.toString();
    }

    /**
     * Get all list info
     * <p>
     * URI: /user-lists/{nick}/follows/{list}
     * </p>
     *
     * @return Si no hay error devuelve todas la info de la playlist{list}.
     */
    @Path("{nick}/follows/{list}")
    @GET
    public String isFollowingList(@PathParam("list") Long listID, @PathParam("nick") String nick) {
        JSONObject result = new JSONObject();

        if(StringUtils.isValid(nick)) {
            if (listID > 0) {
                try (Session s = HibernateUtils.getSession()) {
                    boolean ok = false;
                    Transaction t = s.beginTransaction();
                    EntityUser user = UserCache.getUser(s, nick);
                    if (user != null) {
                        EntitySongList songlist = SongListCache.getSongList(s, listID);
                        if (songlist != null) {
                            if (SQLUtils.getRowCountSQL(s, "song_list_user_follows", "song_list_id = " + songlist.getId() + " and user_id = " + user.getId()) == 1) {
                                result.put("error", "ok");
                                ok = true;
                            } else {
                                result.put("error", "notFollows");
                            }
                        } else {
                            result.put("error", "unknownList");
                        }
                    } else {
                        result.put("error", "unknownUser");
                    }

                    if (ok) {
                        t.commit();
                    } else {
                        t.rollback();
                    }
                }
            } else {
                result.put("error", "invalidList");
            }
        } else {
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
     * @param nick      : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param listId:   List ID
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/delete")
    @DELETE
    public String delete(@PathParam("nick") String nick,
                         @DefaultValue("") @QueryParam("token") String userToken,
                         @QueryParam("listid") long listId) {
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
        } else {
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }

    /**
     * add songs to list
     * <p>
     * URI: /user-lists/{nick}/{list}/add
     * {token:"token"
     * songId:2}
     * </p>
     *
     * @param nick    : Nickname of a user to create the list
     * @param listId: Id de la lista
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/add")
    @POST
    public String add(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
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
                                    if(list.addSong(song)) {
                                        result.put("error", "ok");
                                        ok = true;
                                    }else{
                                        result.put("error", "alreadyAdded");
                                    }
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
        } else {
            result.put("error", "invalidArgs");
        }

        return result.toString();

    }

    /**
     * Delete a list
     * <p>
     * URI: /user-lists/{nick}/{list}/remove
     * {token:"token"
     * songid:2}
     * </p>
     *
     * @param nick    : Nickname of a user to create the list
     * @param listId: Id de la lista
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/remove")
    @POST
    public String remove(@PathParam("nick") String nick, @DefaultValue("") @FormParam("token") String userToken,
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
                                    if(list.removeSong(song)) {
                                        ok = true;
                                        result.put("error", "ok");
                                    }else{
                                        result.put("error", "alreadyRemoved");
                                    }
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
        } else {
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
     * @param nick    : Nickname of a user to create the list
     * @param listId: Id de la lista
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/follow")
    @PUT
    public String addfollower(@PathParam("nick") String nick, @FormParam("token") String userToken,
                              @PathParam("listId") Long listId) {
        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick) && StringUtils.isValid(userToken)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(userToken)) {
                        EntitySongList list = SongListCache.getSongList(s, listId);
                        if(list != null) {
                            if(list.addfollower(user)) {
                                result.put("error", "ok");
                                ok = true;
                            }else{
                                result.put("error", "alreadyAdded");
                            }
                        } else {
                            result.put("error", "unknownList");
                        }
                    } else {
                        result.put("error", "invalidToken");
                    }
                } else{
                    result.put("error", "unknownUser");
                }

                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
            }
        }else {
            result.put("error", "invalidArgs");
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
     * @param nick    : Nickname of a user to create the list
     * @param listId: Id de la lista
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/{listId}/unfollow")
    
    @DELETE
    public String removefollower(@PathParam("nick") String nick, @QueryParam("token") String userToken,
                                 @PathParam("listId") Long listId) {

        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick) && StringUtils.isValid(userToken)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(userToken)) {
                        EntitySongList list = SongListCache.getSongList(s, listId);
                        if(list != null) {
                            if(list.removefollower(user)) {
                                result.put("error", "ok");
                                ok = true;
                            }else{
                                result.put("error", "alreadyRemoved");
                            }
                        } else {
                            result.put("error", "unknownList");
                        }
                    } else {
                        result.put("error", "invalidToken");
                    }
                } else{
                    result.put("error", "unknownUser");
                }

                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
            }
        }else {
            result.put("error", "invalidArgs");
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
     * @param nick : Nickname of a user to create the list
     * @return The result of this query as specified in API.
     */
    @Path("{nick}/following")
    @GET
    public String following(@PathParam("nick") String nick) {

        JSONObject result = new JSONObject();
        if (StringUtils.isValid(nick)) {
            try(Session s = HibernateUtils.getSession()) {
                boolean ok = false;
                Transaction t = s.beginTransaction();
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    Set<EntitySongList> following = user.getFollowing();
                    JSONArray jsonarray = new JSONArray();
                    for (EntitySongList songlist : following) {
                        jsonarray.put(songlist.getId());
                    }
                    result.put("id", jsonarray);
                    result.put("error", "ok");
                    ok = true;
                }else{
                    result.put("error", "unknownUser");
                }

                if(ok){
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
     * Delete a list
     * <p>
     * URI: /user-lists/{listid}/following
     * token
     * </p>
     *
     * @param listID : Nickname of a user to create the list
     * @return The result of this query as specified in API.
     */
    @Path("{listid}/followed")
    
    @GET
    public String followed(@PathParam("listid") Long listID) {
        JSONObject result = new JSONObject();
        if(listID > 0) {
            try(Session s = HibernateUtils.getSession()) {
                boolean ok = false;
                Transaction t = s.beginTransaction();
                EntitySongList sg = SongListCache.getSongList(s, listID);
                if (sg != null) {
                    Set<EntityUser> following = sg.getFollowed();
                    JSONArray jsonarray = new JSONArray();
                    for (EntityUser user : following) {
                        jsonarray.put(user.getId());
                    }
                    result.put("id", jsonarray);
                    result.put("error", "ok");
                    ok = true;
                }else{
                    result.put("error", "unknownSongList");
                }
                if(ok){
                    t.commit();
                }else{
                    t.rollback();
                }
            }
        }
        return result.toString();
    }
}
