package es.eina.sql;

import java.util.ArrayList;
import java.util.List;

public class MySQLQueries {
	private static long LOOKUP_TIME = 2592000000L;
	
	private static List<SQLQueryData> queries = new ArrayList<>();

	public static final QueryId GET_PRODUCT;
	public static final QueryId GET_PRODUCT_COMMENTS;
	public static final QueryId GET_PRODUCT_COMMENT;

	public static final QueryId GET_VENDOR_PRICES_FOR_PRODUCT;
	public static final QueryId DELETE_USER_TOKEN;
	public static final QueryId INSERT_USER;
	public static final QueryId GET_USER_DATA_BY_NAME;
    public static final QueryId GET_USER_TOKEN;
    public static final QueryId UPDATE_USER_TOKEN;
    public static final QueryId DELETE_EXPIRED_TOKENS;
	public static final QueryId GET_USER_COMMENTS;
	public static final QueryId GET_USER_COMMENT;
	public static final QueryId GET_PRODUCTS;
	public static final QueryId GET_PRODUCT_PRICES;
    public static final QueryId GET_USER_ID_FROM_NAME;
    public static final QueryId GET_USER_NAME_FROM_ID;
    public static final QueryId INSERT_COMMENT;

	/**
	 * Search for a query to return its data.
	 * @param id : Id of the query to search.
	 * @return A Query object
	 */
	public static SQLQueryData getQuery(QueryId id){
		return queries.get(id.getQueryId());
	}

	/**
	 * Register a new SQL query.
	 * @param query : SQL query.
	 * @param parameters : SQL query parameters (Number of '?' characters).
	 * @return The Query object of this SQL query.
	 */
	private static QueryId registerQuery(String query, int parameters){
		QueryId qId = new QueryId();
		queries.add(new SQLQueryData(query, parameters));
		return qId;
	}
	
	private static int nextQueryId(){
		return queries.size();
	}
	
	public static class QueryId {
		private int queryId;
		
		private QueryId(){
			this.queryId = MySQLQueries.nextQueryId();
		}
		
		private int getQueryId() {
			return queryId;
		}
	}

	static{

		GET_USER_TOKEN = registerQuery("SELECT * FROM sessions WHERE user_id = ?", 1);
		UPDATE_USER_TOKEN = registerQuery("INSERT INTO sessions (user_id, token, time) VALUES (?, ?, ?) ON CONFLICT (user_id) DO UPDATE SET token = ?, time = ?;", 5);

		///OLD QUERIES
		GET_PRODUCT = registerQuery("SELECT * FROM product WHERE id = ?", 1);
		GET_PRODUCT_COMMENTS = registerQuery("SELECT nick, product_id, opinions.id, opinions.title, opinions.product_mark, opinions.opinion_text FROM opinions INNER JOIN users ON opinions.user_id = users.id WHERE product_id = ? LIMIT ?;", 2);
		GET_PRODUCT_COMMENT = registerQuery("SELECT nick AS nick, product_id, opinions.id, opinions.title, opinions.product_mark, opinions.opinion_text FROM opinions INNER JOIN users ON opinions.user_id = users.id WHERE product_id = ? AND opinions.id = ?;", 2);
		GET_VENDOR_PRICES_FOR_PRODUCT = registerQuery("SELECT vendor_products.price, vendor.id, vendor.url, vendor.vendor_name FROM vendor_products INNER JOIN vendor ON vendor_products.id_vendor = vendor.id WHERE id_product = ? ORDER BY price ASC LIMIT ?;", 2);

		DELETE_USER_TOKEN = registerQuery("DELETE FROM sessions WHERE nick = ?;", 1);
		DELETE_EXPIRED_TOKENS = registerQuery("DELETE FROM sessions WHERE time < ?", 1);

		INSERT_USER = registerQuery("INSERT INTO users(nick, username, mail, pass, birth_date, bio, country, register_date) VALUES(?, ?, ?);", 3);

		GET_USER_DATA_BY_NAME = registerQuery("SELECT * FROM users WHERE nick = ?;", 1);
        GET_USER_COMMENTS = registerQuery("SELECT users.id AS user_id, opinions.id AS opinion_id, opinions.product_id, opinions.title, opinions.product_mark, opinions.opinion_text FROM opinions INNER JOIN users ON opinions.user_id = users.id WHERE users.nick = ? LIMIT ?;", 2);

        GET_USER_COMMENT = registerQuery("SELECT users.id AS user_id, opinions.id AS opinion_id, opinions.product_id, opinions.title, opinions.product_mark, opinions.opinion_text FROM opinions INNER JOIN users ON opinions.user_id = users.id WHERE users.nick = ? AND opinions.id = ?;", 2);
        GET_PRODUCTS = registerQuery("SELECT * FROM product;", 0);

        GET_USER_NAME_FROM_ID = registerQuery("SELECT nick FROM users WHERE id = ?;", 1);
        GET_USER_ID_FROM_NAME = registerQuery("SELECT id FROM users WHERE nick = ?;", 1);
        INSERT_COMMENT = registerQuery("INSERT INTO opinions(title, opinion_text, product_mark, user_id, product_id) VALUES (?, ?, ?, ?, ?);", 5);
        GET_PRODUCT_PRICES = registerQuery("SELECT v.vendor_name, vp.price, vp.id_product FROM vendor_products vp INNER JOIN vendor v ON vp.id_vendor = v.id;", 0);
	}

}
