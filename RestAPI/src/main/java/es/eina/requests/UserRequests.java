package es.eina.requests;

import es.eina.RestApp;
import es.eina.cache.FeedCache;
import es.eina.cache.PopularSongCache;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.search.IndexUsers;
import es.eina.sql.entities.EntityToken;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.StringUtils;
import es.eina.utils.UserUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.sql.Date;
import java.util.List;
import java.util.Set;

@Path("/users/")
@Produces(MediaType.APPLICATION_JSON)
public class UserRequests {

    private static final int DEFAULT_FEED_FOLLOW_NUMBER = 15;
    private static final int DEFAULT_FEED_REPRODUCTION_NUMBER = 15;
    private static final int DEFAULT_FEED_SONGS_NUMBER = 15;
    private static final int USER_SEARCH_NUMBER = 10;
    private static final long USER_SEARCH_MIN_TIME = 0;
    private static final long USER_SEARCH_MAX_TIME = Long.MAX_VALUE;

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    private static final int DEFAULT_COMMENT_NUMBER = 10;

    private static final JSONObject defaultUserJSON;
    private static final JSONObject defaultCommentJSON;

    private static final EmailValidator mailValidator = EmailValidator.getInstance();

    /**
     * Try login a user in the system.
     * <p>
     * URI: /users/{nick}/login
     * </p>
     *
     * @param nick : Username of a user to search
     * @param pass : Password of the User whose username is provided.
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}/login")
    @POST
    public String login(@PathParam("nick") String nick, @FormParam("pass") String pass) {
        JSONObject response = new JSONObject();
        response.put("user", nick);
        response.put("token", "");
        response.put("error", "");

        if (StringUtils.isValid(nick) && StringUtils.isValid(pass)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                if (UserUtils.userExists(s, nick)) {
                    EntityUser user = UserCache.getUser(s, nick);
                    if (UserUtils.checkPassword(user, pass)) {
                        user.updateToken();
                        response.put("token", user.getToken().getToken());
                        response.put("error", "ok");
                        ok = true;
                    } else {
                        response.put("error", "passError");
                    }
                } else {
                    response.put("error", "userNotExists");
                }
                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            response.put("error", "invalidArgs");
        }


        return response.toString();
    }

    /**
     * Try register and login a nick in the system.
     * <p>
     * URI: /users/{nick}/signup
     * </p>
     *
     * @param nick  : Nickname of a user to register (unique)
     * @param pass0 : Password of the User whose username is provided.
     * @param pass1 : Same password as pass0.
     * @param mail  : Email of this username.
     * @param user  : User full name (not unique).
     * @param birth : User birth date (in dd/mm/yyyy format).
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}/signup")
    @POST
    public String signup(@PathParam("nick") String nick, @FormParam("mail") String mail,
                         @FormParam("pass0") String pass0, @FormParam("pass1") String pass1,
                         @FormParam("user") String user, @FormParam("birth") long birth, @FormParam("bio") String bio) {
        JSONObject response = new JSONObject();
        response.put("token", "");
        response.put("error", "");
        //String birth_date = StringUtils.isValid(birth) ? StringUtils.isDate(birth) : null;

        if (StringUtils.isValid(nick, 3, 32) && StringUtils.isValid(mail) && StringUtils.isValid(pass0) &&
                StringUtils.isValid(pass1)) {
            if (mailValidator.isValid(mail)) {
                Date birth_date = new Date(birth);
                if (!StringUtils.isValid(bio)) bio = "";
                if (pass0.equals(pass1)) {
                    try (Session s = HibernateUtils.getSession()) {
                        Transaction t = s.beginTransaction();
                        boolean ok = false;
                        if (!UserUtils.userExists(s, nick)) {
                            String country = Geolocalizer.getCountryISOCode(getIP());
                            EntityUser userData = UserUtils.addUser(s, nick, mail, pass0, user, bio, birth_date, country);
                            if (userData != null) {
                                response.put("token", userData.getToken().getToken());
                                response.put("error", "ok");
                                ok = true;
                            } else {
                                response.put("error", "unknownError");
                            }
                        } else {
                            response.put("error", "userExists");
                        }
                        if (ok) {
                            t.commit();
                        } else {
                            t.rollback();
                        }
                    }
                } else {
                    response.put("error", "notEqualPass");
                }
            } else {
                response.put("error", "wrongMail");
            }
        } else {
            response.put("error", "invalidArgs");
        }


        return response.toString();
    }

    private String getIP() {
        return request != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    /**
     * Try logout a user from the system.
     * <p>
     * URI: /users/{user}/login
     * </p>
     *
     * @param nick  : Username of a user to search
     * @param token : Token string of this User.
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}/login")
    @DELETE
    public String deleteLogin(@PathParam("nick") String nick,
                              @DefaultValue("") @FormParam("token") String token) {
        JSONObject obj = new JSONObject();

        if (StringUtils.isValid(nick) && StringUtils.isValid(token)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(token)) {
                        int code = user.deleteToken(s);
                        if (code == 0) {
                            obj.put("error", "ok");
                            ok = true;
                        } else if (code == -2) {
                            obj.put("error", "closedSession");
                        } else {
                            obj.put("error", "unknownError");
                        }
                    } else {
                        obj.put("error", "invalidToken");
                    }
                } else {
                    obj.put("error", "unknownUser");
                }
                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }

        return obj.toString();
    }

    /**
     * Perform a search for a user.
     * <p>
     * URI: /users/{user}
     * </p>
     *
     * @param nick  : Nick of a user to search
     * @param token : Token string of this user.
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}")
    @GET
    public String getUserData(
            @PathParam("nick") String nick,
            @DefaultValue("") @QueryParam("token") String token
    ) {
        JSONObject obj = new JSONObject();
        JSONObject userJSON = new JSONObject(defaultUserJSON, JSONObject.getNames(defaultUserJSON));
        try (Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            boolean ok = false;
            EntityUser user = UserCache.getUser(s, nick);
            if (user != null) {
                userJSON.put("id", user.getId());
                userJSON.put("nick", user.getNick());
                userJSON.put("user", user.getUsername());
                userJSON.put("bio", user.getBio());
                userJSON.put("verified", user.isVerified());
                userJSON.put("facebook", user.getFacebook());
                userJSON.put("twitter", user.getTwitter());
                userJSON.put("instagram", user.getInstagram());
                if (user.getToken().isValid(token)) {
                    userJSON.put("mail_visible", true);
                    userJSON.put("mail", user.getMail());
                    userJSON.put("country", user.getCountry());
                    userJSON.put("birth_date", user.getBirthDate());
                    userJSON.put("register_date", user.getRegisterDate());
                }
                obj.put("error", false);
                ok = true;
            } else {
                obj.put("error", true);
            }

            if (ok) {
                t.commit();
            } else {
                t.rollback();
            }
        }

        obj.put("profile", userJSON);

        return obj.toString();
    }

    /**
     * Perform a search for a user.
     * <p>
     * URI: /users/{user}
     * </p>
     *
     * @param id  : ID of a user to search
     * @param token : Token string of this user.
     * @return The result of this search as specified in API.
     */
    @Path("/{id}/id")
    @GET
    public String getUserData(
            @PathParam("id") int id,
            @DefaultValue("") @QueryParam("token") String token
    ) {
        JSONObject obj = new JSONObject();
        JSONObject userJSON = new JSONObject(defaultUserJSON, JSONObject.getNames(defaultUserJSON));
        try (Session s = HibernateUtils.getSession()) {
            Transaction t = s.beginTransaction();
            boolean ok = false;
            EntityUser user = UserCache.getUser(s, id);
            if (user != null) {
                userJSON.put("id", user.getId());
                userJSON.put("nick", user.getNick());
                userJSON.put("user", user.getUsername());
                userJSON.put("bio", user.getBio());
                userJSON.put("verified", user.isVerified());
                userJSON.put("facebook", user.getFacebook());
                userJSON.put("twitter", user.getTwitter());
                userJSON.put("instagram", user.getInstagram());
                if (user.getToken().isValid(token)) {
                    userJSON.put("mail_visible", true);
                    userJSON.put("mail", user.getMail());
                    userJSON.put("country", user.getCountry());
                    userJSON.put("birth_date", user.getBirthDate());
                    userJSON.put("register_date", user.getRegisterDate());
                }
                obj.put("error", false);
                ok = true;
            } else {
                obj.put("error", true);
            }

            if (ok) {
                t.commit();
            } else {
                t.rollback();
            }
        }

        obj.put("profile", userJSON);

        return obj.toString();
    }

    @Path("/{nick}/verify")
    @POST
    public String verifyAccount(@PathParam("nick") String nick,
                                @FormParam("self") String adminUser,
                                @DefaultValue("") @FormParam("token") String token,
                                @FormParam("verify") boolean verify) {
        JSONObject obj = new JSONObject();

        if (StringUtils.isValid(nick) && StringUtils.isValid(adminUser) && StringUtils.isValid(token)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                EntityUser admin = UserCache.getUser(s, adminUser);
                if (user != null && admin != null) {
                    EntityToken adminToken = admin.getToken();
                    if (adminToken != null) {
                        if (adminToken.isValid(token)) {
                            if (admin.isAdmin()) {
                                if (verify) {
                                    user.verifyAccount();
                                    obj.put("error", "ok");
                                    ok = true;
                                } else {
                                    int code = user.unverifyAccount();
                                    if (code == 0) {
                                        obj.put("error", "ok");
                                        ok = true;
                                    } else if (code == -1) {
                                        obj.put("error", "unknownError");
                                    } else if (code == -2) {
                                        obj.put("error", "cannotUnverify");
                                    }
                                }
                            } else {
                                obj.put("error", "noPermission");
                            }
                        } else {
                            obj.put("error", "invalidToken");
                        }
                    } else {
                        obj.put("error", "closedSession");
                    }
                } else {
                    obj.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }

        return obj.toString();
    }

    @Path("/{nick}/popular_songs")
    @GET
    public String popular_songs(@PathParam("nick") String nick,
                                @DefaultValue("") @QueryParam("token") String token,
                                @DefaultValue("" + SongRequests.MAX_POPULAR_SONGS) @QueryParam("n") int amount) {
        JSONObject obj = new JSONObject();

        if (StringUtils.isValid(nick) && StringUtils.isValid(token)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    EntityToken userToken = user.getToken();
                    if (userToken != null) {
                        if (userToken.isValid(token)) {
                            amount = Math.max(0, Math.min(SongRequests.MAX_POPULAR_SONGS, amount));
                            obj = PopularSongCache.getPopularSongs(s, amount, user.getCountry());
                            obj.put("error", "ok");
                            t.commit();
                            return obj.toString();
                        } else {
                            obj.put("error", "invalidToken");
                        }
                    } else {
                        obj.put("error", "closedSession");
                    }
                } else {
                    obj.put("error", "unknownUser");
                }
                t.rollback();
            }
        } else {
            obj.put("error", "invalidArgs");
        }

        return obj.toString();
    }

    @Path("/{nick}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public String putUserData(
            @PathParam("nick") String nick,
            @DefaultValue("") @QueryParam("token") String token,
            @DefaultValue("{}") @QueryParam("body") String c
    ) {
        JSONObject result = new JSONObject();

        if (StringUtils.isValid(c) && StringUtils.isValid(token)) {
            JSONObject obj = new JSONObject(c);
            if (obj.has("updates")) {
                JSONObject updateObject = null;
                try {
                    updateObject = obj.getJSONObject("updates");
                } catch (JSONException ex) {
                    result.put("error", "parseError");
                }

                if (updateObject != null) {
                    try (Session s = HibernateUtils.getSession()) {
                        Transaction t = s.beginTransaction();
                        boolean ok = false;
                        EntityUser user = UserCache.getUser(s, nick);
                        if (user != null) {
                            if (user.getToken() != null) {
                                if (user.getToken().isValid(token)) {
                                    JSONObject updateResult = new JSONObject();
                                    boolean dirty = false;
                                    for (String key : updateObject.keySet()) {
                                        Object data = updateObject.get(key);
                                        int code = user.updateUser(key, data);
                                        if (code == 0) {
                                            dirty = true;
                                            updateResult.put(key, "ok");
                                            ok = true;
                                        } else if (code == -1) {
                                            updateResult.put(key, "invalidValue");
                                        } else if (code == -2) {
                                            updateResult.put(key, "passError");
                                        }
                                    }

                                    if (dirty) {
                                        HibernateUtils.addEntityToDB(s, user);
                                    }

                                    result.put("error", updateResult);
                                } else {
                                    result.put("error", "invalidToken");
                                }
                            } else {
                                result.put("error", "closedSession");
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
                }
            } else {
                result.put("error", "noUpdate");
            }
        } else {
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }

    @Path("/delete/{nick}")
    @DELETE
    public String deleteUserData(
            @PathParam("nick") String nick,
            @DefaultValue("") @FormParam("token") String token
    ) {
        JSONObject obj = new JSONObject();

        if (StringUtils.isValid(token)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    if (user.getToken() != null && user.getToken().isValid(token)) {
                        if (UserCache.deleteUser(s, user)) {
                            obj.put("error", "ok");
                            ok = true;
                        } else {
                            obj.put("error", "unknownError");
                        }
                    } else {
                        obj.put("error", "invalidToken");
                    }
                } else {
                    obj.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }

        return obj.toString();
    }

    /**
     * Perform a search of the user comments.
     * <p>
     * URI: /users/{user}/comments[?n={amount}]
     * </p>
     *
     * @param nick    : Username of a user to search
     * @param nFollow : Maximum amount of user follows to return.
     * @param nRepr   : Maximum amount of user reproductions to return.
     * @param nSongs  : Maximum amount of user songs to return.
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}/feed")
    @GET
    public String getFeed(
            @PathParam("nick") String nick,
            @DefaultValue("" + DEFAULT_FEED_FOLLOW_NUMBER) @QueryParam("nFollow") int nFollow,
            @DefaultValue("" + DEFAULT_FEED_REPRODUCTION_NUMBER) @QueryParam("nRepr") int nRepr,
            @DefaultValue("" + DEFAULT_FEED_SONGS_NUMBER) @QueryParam("nSongs") int nSongs
    ) {
        JSONObject obj = new JSONObject();

        if (nFollow > 0 && nRepr > 0 && nSongs > 0) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                EntityUser user = UserCache.getUser(s, nick);
                if (user != null) {
                    obj = FeedCache.getFeed(s, user, nFollow, nRepr, nSongs);
                    obj.put("error", "ok");
                    t.commit();
                } else {
                    obj.put("error", "unknownUser");
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }

        return obj.toString();
    }

    @Path("/{user}/songs")
    @GET
    public String getUserSongs(@PathParam("user") String user) {
        JSONObject obj = new JSONObject();
        if (StringUtils.isValid(user)) {
            try (Session s = HibernateUtils.getSession()) {
                boolean ok = false;
                Transaction t = s.beginTransaction();
                EntityUser myuser = UserCache.getUser(s, user);
                if (myuser != null) {
                    JSONArray userSongs = myuser.getUserSongs();
                    obj.put("songs", userSongs);
                    obj.put("size", userSongs.length());
                    obj.put("error", "ok");
                    ok = true;
                } else {
                    obj.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }
        return obj.toString();
    }

    @Path("/{user}/albums")
    @GET
    public String getUserAlbums(@PathParam("user") String user) {
        JSONObject obj = new JSONObject();
        if (StringUtils.isValid(user)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser myuser = UserCache.getUser(s, user);
                if (myuser != null) {
                    JSONArray userAlbums = myuser.getUserAlbums();
                    obj.put("albums", userAlbums);
                    obj.put("size", userAlbums.length());
                    obj.put("error", "ok");
                    ok = true;
                } else {
                    obj.put("error", "unknownUser");
                }

                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            obj.put("error", "invalidArgs");
        }
        return obj.toString();
    }

    @Path("/{nick}/songs/lastListened")
    @GET
    public String getLastListenedSongs(@PathParam("nick") String nick, @QueryParam("n") @DefaultValue("1") int amount) {
        JSONObject obj = new JSONObject();
        if (StringUtils.isValid(nick)) {
            if (amount > 0) {
                try (Session s = HibernateUtils.getSession()) {
                    Transaction t = s.beginTransaction();
                    boolean ok = false;
                    EntityUser user = UserCache.getUser(s, nick);
                    if (user != null) {
                        JSONArray songs = SongUtils.getLastListenedSongs(s, user, amount);
                        obj.put("songs", songs);
                        obj.put("size", songs.length());
                        obj.put("error", "ok");
                        ok = true;
                    } else {
                        obj.put("error", "unknownUser");
                    }

                    if (ok) {
                        t.commit();
                    } else {
                        t.rollback();
                    }
                }
            } else {
                obj.put("error", "invalidAmount");
            }
        } else {
            obj.put("error", "invalidArgs");
        }
        return obj.toString();
    }


    /**
     * Perform a search of products in the database.<br>
     * <p>
     * URI: /songs/search/?query=[&n={number}][&country={country}][&genre={genre}][&min_time={min_time}][&max_time={max_time}]
     * </p>
     *
     * @param number   : Number of results to return
     * @param keywords : Keywords to search
     * @return The result of this search as specified in API.
     */
    @Path("/search")
    @GET
    public String searchUsers(
            @DefaultValue("" + USER_SEARCH_NUMBER) @QueryParam("n") int number,
            @DefaultValue("") @QueryParam("query") String keywords,
            @DefaultValue("") @QueryParam("country") String country,
            @DefaultValue("" + USER_SEARCH_MIN_TIME) @QueryParam("min_birth_time") long minBirthTime,
            @DefaultValue("" + USER_SEARCH_MAX_TIME) @QueryParam("max_birth_time") long maxBirthTime,
            @DefaultValue("" + USER_SEARCH_MIN_TIME) @QueryParam("min_reg_time") long minRegTime,
            @DefaultValue("" + USER_SEARCH_MAX_TIME) @QueryParam("max_reg_time") long maxRegTime
    ) {
        minBirthTime = Math.max(USER_SEARCH_MIN_TIME, minBirthTime);
        minRegTime = Math.max(USER_SEARCH_MIN_TIME, minRegTime);

        JSONObject obj = new JSONObject();
        JSONObject searchParams = new JSONObject();
        JSONArray users = new JSONArray();

        searchParams.put("query", keywords);
        searchParams.put("min_birth_time", minBirthTime);
        searchParams.put("max_birth_time", maxBirthTime);
        searchParams.put("min_reg_time", minRegTime);
        searchParams.put("max_reg_time", maxRegTime);

        IndexUsers index = RestApp.getUsersIndex();
        index.setSearchParams(country, minRegTime, maxRegTime, minBirthTime, maxBirthTime);
        List<ScoreDoc> result = index.search(keywords, number);

        if (result != null) {
            for (ScoreDoc score : result) {
                Document doc = index.getDocument(score.doc);
                float luceneScore = score.score;

                JSONObject user = new JSONObject(defaultUserJSON, JSONObject.getNames(defaultUserJSON));
                user.put("id", doc.get(IndexUsers.ID_INDEX_COLUMN));
                user.put("nick", doc.get(IndexUsers.NICK_INDEX_COLUMN));
                user.put("user", doc.get(IndexUsers.USERNAME_INDEX_COLUMN));
                user.put("bio", doc.get(IndexUsers.BIO_INDEX_COLUMN));
                user.put("country", doc.get(IndexUsers.COUNTRY_INDEX_COLUMN));
                user.put("birth_date", doc.get(IndexUsers.BIRTH_TIME_INDEX_COLUMN));
                user.put("register_date", doc.get(IndexUsers.REGISTER_TIME_INDEX_COLUMN));
                user.put("score", luceneScore);

                users.put(user);
            }
            obj.put("number", result.size());
        } else {
            obj.put("number", 0);
        }

        obj.put("params", searchParams);
        obj.put("users", users);
        return obj.toString();
    }

    /**
     * Try follow a user in the system.
     * <p>
     * URI: /users/{nick}/follow
     * </p>
     *
     * @param nick  : Nickname of a user to register (unique)
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}/follow/{nick2}")
    @POST
    public String follow(@PathParam("nick") String nick, @PathParam("nick2") String nick2) {
        JSONObject response = new JSONObject();
        response.put("error", "");
        //String birth_date = StringUtils.isValid(birth) ? StringUtils.isDate(birth) : null;

        if (StringUtils.isValid(nick) && StringUtils.isValid(nick2)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user1 = UserCache.getUser(s, nick);
                if (user1 != null) {
                    EntityUser user2 = UserCache.getUser(s, nick2);
                    if (user2 != null) {
                        if (user1.followUser(user2)) {
                            response.put("error", "ok");
                            ok = true;
                        } else {
                            response.put("error", "alreadyFollowing");
                        }
                    }else{
                        response.put("error", "user2NotExists");
                    }
                } else {
                    response.put("error", "user1NotExists");
                }
                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            response.put("error", "invalidArgs");
        }


        return response.toString();
    }

    /**
     * Try follow a user in the system.
     * <p>
     * URI: /users/{nick}/follow
     * </p>
     *
     * @param nick  : Nickname of a user to register (unique)
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}/unfollow/{nick2}")
    @POST
    public String unfollow(@PathParam("nick") String nick, @PathParam("nick2") String nick2) {
        JSONObject response = new JSONObject();
        response.put("error", "");
        //String birth_date = StringUtils.isValid(birth) ? StringUtils.isDate(birth) : null;

        if (StringUtils.isValid(nick) && StringUtils.isValid(nick2)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user1 = UserCache.getUser(s, nick);
                if (user1 != null) {
                    EntityUser user2 = UserCache.getUser(s, nick2);
                    if (user2 != null) {
                        if (user1.unFollowUser(s, user2)) {
                            response.put("error", "ok");
                            ok = true;
                        } else {
                            response.put("error", "unknownError");
                        }
                    }else{
                        response.put("error", "user2NotExists");
                    }
                } else {
                    response.put("error", "user1NotExists");
                }
                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            response.put("error", "invalidArgs");
        }


        return response.toString();
    }

    /**
     * Try follow a user in the system.
     * <p>
     * URI: /users/{nick}/follow
     * </p>
     *
     * @param nick  : Nickname of a user to register (unique)
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}/followers")
    @POST
    public String getFollowers(@PathParam("nick") String nick) {
        JSONObject response = new JSONObject();
        JSONArray usersJSON = new JSONArray();
        response.put("error", "");
        //String birth_date = StringUtils.isValid(birth) ? StringUtils.isDate(birth) : null;

        if (StringUtils.isValid(nick)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user1 = UserCache.getUser(s, nick);
                if (user1 != null) {
                    Set<EntityUser> users = user1.getFollowers();
                    for(EntityUser user : users){
                        usersJSON.put(user.getId());
                    }
                    response.put("size", users.size());
                    response.put("error", "ok");
                    ok = true;
                } else {
                    response.put("error", "unknownUser");
                }
                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            response.put("error", "invalidArgs");
        }
        response.put("users", usersJSON);


        return response.toString();
    }

    /**
     * Try follow a user in the system.
     * <p>
     * URI: /users/{nick}/follow
     * </p>
     *
     * @param nick  : Nickname of a user to register (unique)
     * @return The result of this search as specified in API.
     */
    @Path("/{nick}/follows/{nick2}")
    @POST
    public String follows(@PathParam("nick") String nick, @PathParam("nick2") String nick2) {
        JSONObject response = new JSONObject();
        JSONArray usersJSON = new JSONArray();
        response.put("error", "");
        //String birth_date = StringUtils.isValid(birth) ? StringUtils.isDate(birth) : null;

        if (StringUtils.isValid(nick) && StringUtils.isValid(nick2)) {
            try (Session s = HibernateUtils.getSession()) {
                Transaction t = s.beginTransaction();
                boolean ok = false;
                EntityUser user1 = UserCache.getUser(s, nick);
                if (user1 != null) {
                    EntityUser user2 = UserCache.getUser(s, nick2);
                    if (user2 != null) {
                        if(!user1.getId().equals(user2.getId())) {
                            Set<EntityUser> users = user2.getFollowers();
                            for (EntityUser follower : users) {
                                if (follower.getId().equals(user1.getId())) {
                                    ok = true;
                                    break;
                                }
                            }
                            response.put("error", ok ? "ok" : "notFollows");
                            ok = true;
                        } else {
                            response.put("error", "sameUser");
                        }
                    } else {
                        response.put("error", "user2NotExists");
                    }
                } else {
                    response.put("error", "user1NotExists");
                }
                if (ok) {
                    t.commit();
                } else {
                    t.rollback();
                }
            }
        } else {
            response.put("error", "invalidArgs");
        }
        response.put("users", usersJSON);


        return response.toString();
    }



    static {
        defaultUserJSON = new JSONObject();
        defaultUserJSON.put("id", -1);
        defaultUserJSON.put("nick", "");
        defaultUserJSON.put("user", "");
        defaultUserJSON.put("mail_visible", false);
        defaultUserJSON.put("mail", "");
        defaultUserJSON.put("bio", "");
        defaultUserJSON.put("country", Geolocalizer.DEFAULT_COUNTRY);
        defaultUserJSON.put("birth_date", -1);
        defaultUserJSON.put("register_date", -1);
        defaultUserJSON.put("facebook", "");
        defaultUserJSON.put("twitter", "");
        defaultUserJSON.put("instagram", "");

        defaultCommentJSON = new JSONObject();
        defaultCommentJSON.put("id", -1);
        defaultCommentJSON.put("product", -1);
        defaultCommentJSON.put("user", "");
        defaultCommentJSON.put("title", "");
        defaultCommentJSON.put("rate", -1.0);
        defaultCommentJSON.put("text", "");
    }
}
