package es.eina.requests;

import es.eina.cache.UserCache;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.StringUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/user-lists/")
@Produces(MediaType.APPLICATION_JSON)
public class UserSongListRequests {

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    /**
     * Try register and login a nick in the system.
     * <p>
     * URI: /user-lists/create
     * </p>
     *
     * @param nick  : Nickname of a user to create the list
     * @param userToken : User private token.
     * @param title : List title
     * @return The result of this query as specified in API.
     */
    @Path("/create")
    @POST
    public String create(@FormParam("nick") String nick, @FormParam("token") String userToken,
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

}
