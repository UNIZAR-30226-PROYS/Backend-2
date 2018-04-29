package es.eina.requests;

import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/version/")
@Produces(MediaType.APPLICATION_JSON)
public class VersionRequests {

    private static final String VERSION_NUMBER = "v1.0.0";
    private static final int MAYOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;
    private static final int MICRO_VERSION = 0;

    @Path("/")
    @GET
    public static String getVersionNumber(){
        JSONObject object = new JSONObject();

        object.put("version", VERSION_NUMBER);
        object.put("mayor", MAYOR_VERSION);
        object.put("minor", MINOR_VERSION);
        object.put("micro", MICRO_VERSION);

        return object.toString();
    }

}
