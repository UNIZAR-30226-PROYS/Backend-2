package es.eina.filter;


import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.ext.Provider;

@Provider
public class ResponseFilter implements ContainerResponseFilter{

	@Override
	public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {
		containerResponse.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
		return containerResponse;
	}
}
