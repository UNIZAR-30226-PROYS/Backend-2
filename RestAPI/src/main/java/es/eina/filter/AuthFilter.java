package es.eina.filter;

import es.eina.RestApp;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
@AuthRequired
public class AuthFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        containerResponseContext.getHeaders().putSingle("Random", "true");
        //RestApp.getInstance().getLogger().info("Auth required for " + containerRequestContext.getUriInfo().toString());
    }
}
