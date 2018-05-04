package es.eina;


import es.eina.search.Index;
import es.eina.search.IndexProduct;
import es.eina.search.IndexSongs;
import es.eina.sql.MySQLConnection;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class RestApp {

	private static RestApp instance;
	//private static MySQLConnection sql = new MySQLConnection("host", "user", "pass", "db");
    private static MySQLConnection sql = new MySQLConnection("dev.langelp.net", "postgres", "Admin123", "postgres");
	private final Logger logger = Logger.getLogger("webLogger");
	private static final Index index = new IndexProduct();
	private static final IndexSongs songsIndex = new IndexSongs();

	public RestApp() {
		instance = this;
		//index.openIndex("index/");
        try {
            new File("indices/songIndex/").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        songsIndex.openIndex("indices/songIndex/");

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

	public static Index getIndex() {
		return index;
	}

	public static IndexSongs getSongsIndex() {
		return songsIndex;
	}
}
