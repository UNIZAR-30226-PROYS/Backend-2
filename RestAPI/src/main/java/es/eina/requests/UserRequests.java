package es.eina.requests;

import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.entities.EntityToken;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import es.eina.utils.SongUtils;
import es.eina.utils.StringUtils;
import es.eina.utils.UserUtils;
import org.apache.commons.validator.routines.EmailValidator;
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

@Path("/users/")
@Produces(MediaType.APPLICATION_JSON)
public class UserRequests {

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

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
    public JSONObject login(@PathParam("nick") String nick, @FormParam("pass") String pass) {
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


        return response;
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
    public JSONObject signup(@PathParam("nick") String nick, @FormParam("mail") String mail,
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


        return response;
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
    public JSONObject deleteLogin(@PathParam("nick") String nick,
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

        return obj;
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
    public JSONObject getUserData(
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

        return obj;
    }

    @Path("/{nick}/verify")
    @POST
    public JSONObject verifyAccount(@PathParam("nick") String nick,
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

        return obj;
    }

    @Path("/{nick}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public JSONObject putUserData(
            @PathParam("nick") String nick,
            @DefaultValue("") @QueryParam("token") String token,
            String c
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

        return result;
    }

    @Path("/{nick}")
    @DELETE
    public JSONObject deleteUserData(
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

        return obj;
    }

    @Path("/{user}/songs")
    @GET
    public JSONObject getUserSongs(@PathParam("user") String user) {
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
        return obj;
    }

    @Path("/{user}/albums")
    @GET
    public JSONObject getUserAlbums(@PathParam("user") String user) {
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
        return obj;
    }

    @Path("/{nick}/songs/lastListened")
    @GET
    public JSONObject getLastListenedSongs(@PathParam("nick") String nick, @QueryParam("n") @DefaultValue("1") int amount) {
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
        return obj;
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

        defaultCommentJSON = new JSONObject();
        defaultCommentJSON.put("id", -1);
        defaultCommentJSON.put("product", -1);
        defaultCommentJSON.put("user", "");
        defaultCommentJSON.put("title", "");
        defaultCommentJSON.put("rate", -1.0);
        defaultCommentJSON.put("text", "");
    }
}
