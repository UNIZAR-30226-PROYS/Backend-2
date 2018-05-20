package es.eina.requests;

import es.eina.cache.AlbumCache;
import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.listener.Listener;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/debug/")
public class DebugRequests {

    @GET
    @Path("forceSave/")
    public String forceCacheSave(){
        UserCache.forceSave();
        SongCache.forceSave();
        AlbumCache.forceSave();
        return "OK";
    }
}
