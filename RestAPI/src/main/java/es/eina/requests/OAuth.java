package es.eina.requests;


import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.PeopleFeed;
import com.google.gson.Gson;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.entities.EntityUser;
import es.eina.utils.UserUtils;
import org.json.JSONObject;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.*;
import java.sql.Date;

@Path("/oauth/")
@Produces(MediaType.APPLICATION_JSON)
public class OAuth {

    @Context
    private HttpServletRequest request;
    /*
     * Default HTTP transport to use to make HTTP requests.
     */
    private static final HttpTransport TRANSPORT = new NetHttpTransport();

    /*
     * Default JSON factory to use to deserialize JSON.
     */
    private static final JacksonFactory JSON_FACTORY = new JacksonFactory();

    /*
     * Gson object to serialize JSON responses to requests to this servlet.
     */
    private static final Gson GSON = new Gson();

    /*
     * Creates a client secrets object from the client_secrets.json file.
     */
    private static GoogleClientSecrets clientSecrets;

    static {
        try {
            Reader reader = new FileReader("client_secrets.json.example");
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        } catch (IOException e) {
            throw new Error("No client_secrets.json found", e);
        }
    }

    /*
     * This is the Client ID that you generated in the API Console.
     */
    private static final String CLIENT_ID = clientSecrets.getWeb().getClientId();

    /*
     * This is the Client Secret that you generated in the API Console.
     */
    private static final String CLIENT_SECRET = clientSecrets.getWeb().getClientSecret();

    /*
     * Optionally replace this with your application's name.
     */
    private static final String APPLICATION_NAME = "Google+ Java Quickstart";

    /**
     * Upgrade given auth code to token, and store it in the session.
     * POST body of request should be the authorization code.
     * Example URI: /login?code=
     */
    @GET
    @Path("/login")
    public String login(@FormParam("token") String authCode) throws IOException {
        JSONObject response = new JSONObject();
        // Only connect a user that is not already connected.
        if (authCode == null) {
            response.put("error","ErrorArgs");
            return response.toString();
        }
        String CLIENT_SECRET_FILE = "client_secrets.json";
        String REDIRECT_URI = "";
        // Exchange auth code for access token
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(
                        com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(), new FileReader(CLIENT_SECRET_FILE));
        GoogleTokenResponse tokenResponse =
                new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                        "https://www.googleapis.com/oauth2/v4/token",
                        clientSecrets.getDetails().getClientId(),
                        clientSecrets.getDetails().getClientSecret(),
                        authCode,
                        REDIRECT_URI)  // Specify the same redirect URI that you use with your web
                        // app. If you don't have a web version of your app, you can
                        // specify an empty string.
                        .execute();

        String accessToken = tokenResponse.getAccessToken();
        // Get profile info from ID token
        GoogleIdToken idToken = null;
        try {
            idToken = tokenResponse.parseIdToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String userId = payload.getSubject();  // Use this value as a key to identify a user.
        String email = payload.getEmail();
        boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
        String locale = (String) payload.get("locale");
        String familyName = (String) payload.get("family_name");
        String givenName = (String) payload.get("given_name");

        EntityUser user = UserCache.getUser(email);
        if (user != null) {
            String country = Geolocalizer.getInstance().getCountryCode(request.getRemoteAddr());
            EntityUser userData = UserUtils.addUser(email, email, accessToken, userId, "", new Date(0), country);
            if (userData != null) {
                response.put("token", userData.getToken().getToken());
                response.put("error", "ok");
            } else {
                response.put("error", "unknownError");
            }
        }
        return response.toString();
    }

    /**
     * Upgrade given auth code to token, and store it in the session.
     * POST body of request should be the authorization code.
     * Example URI: /login?token=
     */
    @GET
    @Path("/logout")
    public void logout(@Context HttpServletRequest request,
                      @Context HttpServletResponse response) throws IOException {
        String tokenData = (String) request.getSession().getAttribute("token");
        if (tokenData == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print(GSON.toJson("Current user not connected."));
            return;
        }
        try {
            // Build credential from stored token data.
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setJsonFactory(JSON_FACTORY)
                    .setTransport(TRANSPORT)
                    .setClientSecrets(CLIENT_ID, CLIENT_SECRET).build()
                    .setFromTokenResponse(JSON_FACTORY.fromString(
                            tokenData, GoogleTokenResponse.class));
            // Execute HTTP GET request to revoke current token.
            HttpResponse revokeResponse = TRANSPORT.createRequestFactory()
                    .buildGetRequest(new GenericUrl(
                            String.format(
                                    "https://accounts.google.com/o/oauth2/revoke?token=%s",
                                    credential.getAccessToken()))).execute();
            // Reset the user's session.
            request.getSession().removeAttribute("token");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print(GSON.toJson("Successfully disconnected."));
        } catch (IOException e) {
            // For whatever reason, the given token was invalid.
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(GSON.toJson("Failed to revoke token for given user."));
        }
    }
}