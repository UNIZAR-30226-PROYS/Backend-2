package es.eina;


import es.eina.search.Index;

import java.util.logging.Logger;

public class RestApp {

	private static RestApp instance;
	private final Logger logger = Logger.getLogger("webLogger");

	public RestApp() {
		instance = this;
		//index.openIndex("index/");

	}

	public static RestApp getInstance() {
		return instance;
	}

	public Logger getLogger() {
		return logger;
	}
}
