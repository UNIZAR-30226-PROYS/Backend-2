package es.eina.listener;

import es.eina.RestApp;
import es.eina.cache.UserCache;
import es.eina.geolocalization.Geolocalizer;
import es.eina.sql.utils.HibernateUtils;
import es.eina.task.CleanUpCache;
import es.eina.task.TaskBase;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;
import java.io.InputStream;

public class Listener implements ServletContextListener,
		HttpSessionListener, HttpSessionAttributeListener {

	private RestApp restApp;
	private CleanUpCache cache;

	// Public constructor is required by servlet spec
	public Listener() {
	}

	// -------------------------------------------------------
	// ServletContextListener implementation
	// -------------------------------------------------------

	/**
	 * This method is called when the servlet context is initialized(when the Web application is deployed).
	 * @param sce : Servlet context
	 */
	public void contextInitialized(ServletContextEvent sce) {
	  /* This method is called when the servlet context is
         initialized(when the Web application is deployed). 
         You can initialize servlet context related data here.
      */
	  Geolocalizer.build("/GeoLite2-Country.mmdb");
		InputStream f = getClass().getResourceAsStream("database.properties");
	  HibernateUtils.configureDatabase(f);
	  restApp = new RestApp();
	  cache = new CleanUpCache();

	}

    /**
     * This method is invoked when the Servlet Context (the Web application) is undeployed or Application Server shuts down.
     * @param sce : Servlet context
     */
	public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
		System.out.println("Close MySQL");
		cache.cancel();
		TaskBase.cleanUp();
		HibernateUtils.shutdown();
	}

	// -------------------------------------------------------
	// HttpSessionListener implementation
	// -------------------------------------------------------
	public void sessionCreated(HttpSessionEvent se) {
      /* Session is created. */
	}

	public void sessionDestroyed(HttpSessionEvent se) {
      /* Session is destroyed. */
	}

	// -------------------------------------------------------
	// HttpSessionAttributeListener implementation
	// -------------------------------------------------------

	public void attributeAdded(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute 
         is added to a session.
      */
	}

	public void attributeRemoved(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute
         is removed from a session.
      */
	}

	public void attributeReplaced(HttpSessionBindingEvent sbe) {
      /* This method is invoked when an attibute
         is replaced in a session.
      */
	}


}
