package es.eina.requests;

import es.eina.RestApp;
import es.eina.cache.TokenManager;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.MySQLConnection;
import es.eina.sql.MySQLQueries;
import es.eina.sql.entities.EntityToken;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.parameters.SQLParameterInteger;
import es.eina.sql.parameters.SQLParameterString;
import es.eina.utils.StringUtils;
import es.eina.utils.UserUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/users/")
@Produces(MediaType.APPLICATION_JSON)
public class UserRequests {

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    private static final int DEFAULT_COMMENT_NUMBER = 10;
    private static final int DEFAULT_COMMENT_LIKES_NUMBER = 10;

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
            if (UserUtils.userExists(nick)) {
                EntityUser user = UserCache.getUser(nick);
                if (UserUtils.checkPassword(user, pass)) {
                    user.updateToken();
                    response.put("token", user.getToken().getToken());
                    response.put("error", "ok");
                } else {
                    response.put("error", "passError");
                }
            } else {
                response.put("error", "userNotExists");
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
                StringUtils.isValid(pass1) && StringUtils.isValid(nick)) {
            if(mailValidator.isValid(mail)) {
                Date birth_date = new Date(birth);
                if (!StringUtils.isValid(bio)) bio = "";
                if (pass0.equals(pass1)) {
                    if (!UserUtils.userExists(nick)) {
                        String country = Geolocalizer.getInstance().getCountryCode(request.getRemoteAddr());
                        EntityUser userData = UserUtils.addUser(nick, mail, pass0, user, bio, birth_date, country);
                        if (userData != null) {
                            response.put("token", userData.getToken().getToken());
                            response.put("error", "ok");
                        } else {
                            response.put("error", "unknownError");
                        }
                    } else {
                        response.put("error", "userExists");
                    }
                } else {
                    response.put("error", "notEqualPass");
                }
            }else{
                response.put("error", "wrongMail");
            }
        } else {
            response.put("error", "invalidArgs");
        }


        return response.toString();
    }

    /**
     * Try logout a user from the system.
     * <p>
     * URI: /users/{user}/login
     * </p>
     *
     * @param user  : Username of a user to search
     * @param token : Token string of this User.
     * @return The result of this search as specified in API.
     */
    @Path("/{user}/login")
    @DELETE
    public String deleteLogin(@PathParam("user") String user, @FormParam("token") String token) {
        JSONObject response = new JSONObject();
        boolean error = !UserUtils.validateUserToken(user, token) && !UserUtils.deleteUserToken(user);
        response.put("error", error);
        return response.toString();
    }

    /**
     * Perform a search for a user.
     * <p>
     * URI: /users/{user}
     * </p>
     *
     * @param user  : Username of a user to search
     * @param token : Token string of this user.
     * @return The result of this search as specified in API.
     */
    @Path("/{user}")
    @GET
    public String getUserData(
            @PathParam("user") String user,
            @DefaultValue("") @QueryParam("token") String token
    ) {
        JSONObject obj = new JSONObject();
        JSONObject userJSON = new JSONObject(defaultUserJSON, JSONObject.getNames(defaultUserJSON));
        ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_USER_DATA_BY_NAME, new SQLParameterString(user));

        try {
            if (set.first()) {
                if (UserUtils.validateUserToken(user, token)) {
                    userJSON.put("public_profile", true);
                    userJSON.put("name", set.getString("real_name"));
                    userJSON.put("mail", set.getString("mail"));
                    userJSON.put("mail_visible", true);
                }
                userJSON.put("user", user);
                double valuation = set.getDouble("valuation_amount") / set.getDouble("valuation_sum");
                userJSON.put("public_rate", Double.isFinite(valuation) ? valuation : -1.0);
                userJSON.put("description", set.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            obj.put("error", 1);
        }

        obj.put("profile", userJSON);

        return obj.toString();
    }

    @Path("/{user}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public String putUserData(
            @PathParam("user") String user,
            String c
    ) {
        JSONObject obj = new JSONObject(c);
        return null; //TODO REPLACE
    }

    @Path("/{user}")
    @DELETE
    public String deleteUserData(
            @PathParam("user") String user,
            @FormParam("token") String token
    ) {
        return null; //TODO REPLACE
    }

    /**
     * Perform a search of the user comments.
     * <p>
     * URI: /users/{user}/comments[?n={amount}]
     * </p>
     *
     * @param user   : Username of a user to search
     * @param number : Maximum amount of comments to return.
     * @return The result of this search as specified in API.
     */
    @Path("/{user}/comments")
    @GET
    public String getUserComments(
            @PathParam("user") String user,
            @DefaultValue("" + DEFAULT_COMMENT_NUMBER) @QueryParam("n") int number
    ) {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_USER_COMMENTS, new SQLParameterString(user), new SQLParameterInteger(number));

        try {
            while (set.next()) {
                JSONObject object = new JSONObject(defaultCommentJSON, JSONObject.getNames(defaultCommentJSON));
                object.put("id", set.getString("opinion_id"));
                object.put("product", set.getString("user_id"));
                object.put("user", user);
                object.put("title", set.getString("title"));
                object.put("rate", set.getString("product_mark"));
                object.put("text", set.getString("opinion_text"));

                array.put(object);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            MySQLConnection.closeStatement(set);
        }

        obj.put("user", user);
        obj.put("number", array.length());
        obj.put("comments", array);

        return obj.toString();
    }

    /**
     * Search for a specific comment of a user.
     * <p>
     * URI: /users/{user}/comments/{comment_id}
     * </p>
     *
     * @param user       : Username of a user to search
     * @param comment_id : Id of a comment.
     * @return The result of this search as specified in API.
     */
    @Path("/{user}/comments/{comment_id}")
    @GET
    public String getUserComment(
            @PathParam("user") String user,
            @PathParam("comment_id") int comment_id
    ) {

        JSONObject object = new JSONObject(defaultCommentJSON, JSONObject.getNames(defaultCommentJSON));
        ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_USER_COMMENT, new SQLParameterString(user), new SQLParameterInteger(comment_id));

        try {
            if (set.next()) {
                object.put("id", set.getString("opinion_id"));
                object.put("product", set.getString("user_id"));
                object.put("user", user);
                object.put("title", set.getString("title"));
                object.put("rate", set.getString("product_mark"));
                object.put("text", set.getString("opinion_text"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            MySQLConnection.closeStatement(set);
        }

        return object.toString();
    }

    @Path("/{user}/comments/{comment_id}")
    @DELETE
    public String deleteUserComment(
            @PathParam("user") String user,
            @PathParam("comment_id") int comment_id,
            @FormParam("token") String token
    ) {
        return null; //TODO REPLACE
    }

    @Path("/{user}/comments/{comment_id}/likes")
    @GET
    public String getUserCommentLikes(
            @PathParam("user") String user,
            @PathParam("comment_id") int comment_id,
            @DefaultValue("" + DEFAULT_COMMENT_LIKES_NUMBER) @QueryParam("n") int number
    ) {
        return null; //TODO REPLACE
    }

    @Path("/{user}/comments/{comment_id}/likes/{like_id}")
    @GET
    public String getUserCommentLike(
            @PathParam("user") String user,
            @PathParam("comment_id") int comment_id,
            @PathParam("like_id") int like_id
    ) {
        return null; //TODO REPLACE
    }

    @Path("/{user}/likes")
    @GET
    public String getUserLikes(
            @PathParam("user") String user,
            @DefaultValue("" + DEFAULT_COMMENT_LIKES_NUMBER) @QueryParam("n") int number
    ) {
        return null; //TODO REPLACE
    }

    @Path("/{user}/likes/{like_id}")
    @GET
    public String getUserLike(
            @PathParam("user") String user,
            @PathParam("like_id") int likeId
    ) {
        return null; //TODO REPLACE
    }

    @Path("/{user}/likes/{like_id}")
    @DELETE
    public String deleteUserLikes(
            @PathParam("user") String user,
            @PathParam("like_id") int likeId,
            @FormParam("token") String token
    ) {
        return null; //TODO REPLACE
    }

    static {
        defaultUserJSON = new JSONObject();
        defaultUserJSON.put("public_profile", false);
        defaultUserJSON.put("user", "");
        defaultUserJSON.put("name", "");
        defaultUserJSON.put("mail", "");
        defaultUserJSON.put("mail_visible", false);
        defaultUserJSON.put("public_rate", -1.0);
        defaultUserJSON.put("description", "");

        defaultCommentJSON = new JSONObject();
        defaultCommentJSON.put("id", -1);
        defaultCommentJSON.put("product", -1);
        defaultCommentJSON.put("user", "");
        defaultCommentJSON.put("title", "");
        defaultCommentJSON.put("rate", -1.0);
        defaultCommentJSON.put("text", "");
    }
}
