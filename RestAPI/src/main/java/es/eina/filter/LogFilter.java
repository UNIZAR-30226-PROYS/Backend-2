package es.eina.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class LogFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	public void doFilter(ServletRequest req, ServletResponse res,
						 FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		request.getHeader("");
		HttpServletResponse response = (HttpServletResponse) res;
		response.sendError(403);


		//Get the IP address of client machine.
		String ipAddress = request.getRemoteAddr();

		//Log the IP address and current timestamp.
		System.out.println("IP "+ipAddress + ", Time "
				+ new Date().toString());

		chain.doFilter(req, res);
	}

	@Override
	public void destroy() {

	}

	/*
	public void init(FilterConfig config) throws ServletException {

		//Get init parameter
		//String testParam = config.getInitParameter("test-param");

		//Print the init parameter
		//System.out.println("Test Param: " + testParam);
	}
	public void destroy() {
		//add code to release any resource
	}*/
}
