package es.eina;


import es.eina.recommender.Recommender;
import es.eina.search.Index;
import es.eina.search.IndexPriceAndProduct;
import es.eina.search.IndexProduct;
import es.eina.sql.MySQLConnection;

import java.io.File;
import java.util.logging.Logger;

public class RestApp {

	private static RestApp instance;
	//private static MySQLConnection sql = new MySQLConnection("host", "user", "pass", "db");
    private static MySQLConnection sql = new MySQLConnection("dev.langelp.net", "postgres", "Admin123", "postgres");
	private final Logger logger = Logger.getLogger("webLogger");
	private static final Index index = new IndexProduct();

	private final Recommender recommender;

	public RestApp() {
		instance = this;
		recommender = new Recommender();
		//index.openIndex("index/");

	}

	public static RestApp getInstance() {
		return instance;
	}

	public static MySQLConnection getSql() {
		return sql;
	}

	public Logger getLogger() {
		return logger;
	}

    public Recommender getRecommender() {
        return recommender;
    }

    public static Index getIndex() {
		return index;
	}
}
