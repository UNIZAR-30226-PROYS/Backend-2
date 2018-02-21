package es.eina.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext containerRequest, ContainerResponseContext containerResponse) {
        containerResponse.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        containerResponse.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        containerResponse.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
        containerResponse.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
    }
}
