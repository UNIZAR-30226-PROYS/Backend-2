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


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.*;

@Path("/oauth/")
@Produces(MediaType.APPLICATION_JSON)
public class OAuth {

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
    public void login(@Context HttpServletRequest request,
                          @Context HttpServletResponse response) throws IOException {
        // Only connect a user that is not already connected.
        String tokenData = "4/AADZQSzZ6qIBUlOiL-sFWwCmS55f_YOOXY1rkP_a5luzm4oWFE1_vR7GKRow2ey3BJEI0CTlJ3rr-Jg7IeUN2vo";
        if (tokenData != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print(GSON.toJson("Current user is already connected."));
            return;
        }
        // Ensure that this is no request forgery going on, and that the user
        // sending us this connect request is the user that was supposed to.
        if (!request.getParameter("state").equals(request.getSession().getAttribute("state"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print(GSON.toJson("Invalid state parameter."));
            return;
        }
        // Normally the state would be a one-time use token, however in our
        // simple case, we want a user to be able to connect and disconnect
        // without reloading the page.  Thus, for demonstration, we don't
        // implement this best practice.
        //request.getSession().removeAttribute("state");
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        getContent(request.getInputStream(), resultStream);
        String code = new String(resultStream.toByteArray(), "UTF-8");

        try {
            // Upgrade the authorization code into an access and refresh token.
            GoogleTokenResponse tokenResponse =
                    new GoogleAuthorizationCodeTokenRequest(TRANSPORT, JSON_FACTORY,
                            CLIENT_ID, CLIENT_SECRET, code, "postmessage").execute();

            // You can read the Google user ID in the ID token.
            // This sample does not use the user ID.
            GoogleIdToken idToken = tokenResponse.parseIdToken();
            String gplusId = idToken.getPayload().getSubject();

            // Store the token in the session for later use.
            request.getSession().setAttribute("token", tokenResponse.toString());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print(GSON.toJson("Successfully connected user."));
        } catch (TokenResponseException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(GSON.toJson("Failed to upgrade the authorization code."));
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(GSON.toJson("Failed to read token data from Google. " +
                    e.getMessage()));
        }
    }

    /*
     * Read the content of an InputStream.
     *
     * @param inputStream the InputStream to be read.
     * @return the content of the InputStream as a ByteArrayOutputStream.
     * @throws IOException
     */
    static void getContent(InputStream inputStream, ByteArrayOutputStream outputStream)
            throws IOException {
        // Read the response into a buffered stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int readChar;
        while ((readChar = reader.read()) != -1) {
            outputStream.write(readChar);
        }
        reader.close();
    }
    /**
     * Upgrade given auth code to token, and store it in the session.
     * POST body of request should be the authorization code.
     * Example URI: /login?code=
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