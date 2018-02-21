package es.eina.filter;

import es.eina.RestApp;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.Date;

@Provider
public class LogFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        //Get the IP address of client machine.
        String ipAddress = containerRequest.getHeaderString("REMOTE_ADDR");
        MultivaluedMap<String, String> map = containerRequest.getHeaders();
        for (String k : map.keySet()) {
            RestApp.getInstance().getLogger().info("Key: " + k + " - Value: " + map.get(k));
        }

        //Log the IP address and current timestamp.
        System.out.println("IP " + ipAddress + ", Time "
                + new Date().toString());
    }
}
