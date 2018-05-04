package es.eina.requests;

import es.eina.RestApp;
import es.eina.cache.UserIdCache;
import es.eina.filter.AuthRequired;
import es.eina.search.Index;
import es.eina.search.IndexProduct;
import es.eina.sql.MySQLConnection;
import es.eina.sql.MySQLQueries;
import es.eina.sql.parameters.SQLParameterInteger;
import es.eina.sql.parameters.SQLParameterString;
import es.eina.utils.StringUtils;
import es.eina.utils.UserUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Path("/products/")
@Produces(MediaType.APPLICATION_JSON)
public class ProductRequests {

	private static final int PRODUCT_SEARCH_NUMBER = 10;
	private static final float PRODUCT_SEARCH_MIN_PRICE = Float.MIN_VALUE;
	private static final float PRODUCT_SEARCH_MAX_PRICE = Float.MAX_VALUE;
	private static final int PRODUCT_DEFAULT_COMMENT_NUMBER = 10;
	private static final int PRODUCT_DEFAULT_COMMENT_LIKES_NUMBER = 10;
	private static final int PRODUCT_DEFAULT_VENDOR_NUMBER = 10;

	private static final JSONObject defaultProductJSON;
	private static final JSONObject defaultProductCommentJSON;
	private static final JSONObject defaultProductCommentsJSON;
	private static final JSONObject defaultVendorJSON;
	private static final JSONObject defaultProductVendorJSON;


    /**
     * Perform a search of products in the database.<br>
     *     <p>
     *         URI: /products/search/?keywords=[&n={number}][&category={category}][&min_price={min_price}][&max_price={max_price}]
     *     </p>
     * @param number : Number of results to return
     * @param keywords : Keywords to search
     * @param category : Categories to search [NOT USED]
     * @param minPrice : Minimum price to search [NOT USED]
     * @param maxPrice : Maximum price to search [NOT USED]
     * @return The result of this search as specified in API.
     */
	@Path("/search")
	@GET
	public String searchProducts(
		@DefaultValue("" + PRODUCT_SEARCH_NUMBER) @QueryParam("n") int number,
		@DefaultValue("") @QueryParam("keywords") String keywords,
		@DefaultValue("") @QueryParam("category") String category,
		@DefaultValue("" + PRODUCT_SEARCH_MIN_PRICE) @QueryParam("min_price") float minPrice,
		@DefaultValue("" + PRODUCT_SEARCH_MAX_PRICE) @QueryParam("max_price") float maxPrice
	){
	    minPrice = Math.max(PRODUCT_SEARCH_MIN_PRICE, minPrice);

        JSONObject obj = new JSONObject();
        JSONObject searchParams = new JSONObject();
        JSONArray products = new JSONArray();

        searchParams.put("keywords", keywords);
        searchParams.put("category", category);
        searchParams.put("min_price", minPrice);
        searchParams.put("max_price", maxPrice);

		List<ScoreDoc> result = RestApp.getIndex().search(keywords, number);
		Index index = RestApp.getIndex();

		if(result != null) {
			for (ScoreDoc score : result) {
				Document doc = index.getDocument(score.doc);
				float luceneScore = score.score;

				JSONObject product = new JSONObject(defaultProductJSON, JSONObject.getNames(defaultProductJSON));
				product.put("id", doc.get(IndexProduct.ID_INDEX_COLUMN));
				product.put("name", doc.get(IndexProduct.NAME_INDEX_COLUMN));
				product.put("public_rate", doc.get(IndexProduct.RATE_INDEX_COLUMN));
				product.put("description", doc.get(IndexProduct.DESC_INDEX_COLUMN));
				product.put("score", luceneScore);

				products.put(product);
			}
			obj.put("number", result.size());
		}else{
			obj.put("number", 0);
		}

        obj.put("params", searchParams);
        obj.put("products", products);
		return obj.toString();
	}

    /**
     * Search a product in database
     * <p>
     *  URI: /products/{product_id}
     * </p>
     * @param product : Id of a product to search.
     * @return The result of this search as specified in API.
     */
	@Path("/{product}")
	@GET
	public String getProduct(
			@PathParam("product") int product
	){
		JSONObject obj = new JSONObject();
		JSONObject productJSON = new JSONObject(defaultProductJSON, JSONObject.getNames(defaultProductJSON));

		ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_PRODUCT, new SQLParameterInteger(product));
		try {
			if(set.first()){
                double amount = set.getDouble("valuation_amount");
				productJSON.put("id", set.getInt("id"));
				productJSON.put("name", set.getString("product_name"));
				productJSON.put("public_rate", amount != 0 ? set.getDouble("valuation_sum")/amount : 0.0);
				productJSON.put("description", set.getString("description"));
			}else{
				obj.put("error", 2);
			}
		} catch (SQLException e) {
			obj.put("error", 1);
			e.printStackTrace();
		}finally {
			MySQLConnection.closeStatement(set);
		}

		obj.put("product", productJSON);
		return obj.toString();
	}

    /**
     * Search all comments of a product in database
     * <p>
     *  URI: /products/{product_id}/comments[?n={amount}]
     * </p>
     * @param product : Id of a product to search.
     * @param comments : Maximum number of comments to return.
     * @return The result of this search as specified in API.
     */
	@Path("/{product}/comments")
	@GET
	public String getProductComments(
			@PathParam("product") int product,
			@DefaultValue("" + PRODUCT_DEFAULT_COMMENT_NUMBER) @QueryParam("n") int comments
	){
		JSONObject commentJSON = new JSONObject(defaultProductCommentsJSON, JSONObject.getNames(defaultProductCommentsJSON));
		JSONArray array = new JSONArray();

		ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_PRODUCT_COMMENTS, new SQLParameterInteger(product), new SQLParameterInteger(comments));
		try {
			while(set.next()){
				JSONObject obj = new JSONObject(defaultProductCommentJSON, JSONObject.getNames(defaultProductCommentJSON));
				obj.put("id", set.getInt("id"));
				obj.put("product", set.getInt("product_id"));
				obj.put("user", set.getString("nick"));
				obj.put("title", set.getString("title"));
				obj.put("text", set.getString("opinion_text"));
				obj.put("rate", set.getDouble("product_mark"));
				array.put(obj);
			}
		} catch (SQLException e) {
			commentJSON.put("error", 1);
			e.printStackTrace();
		}finally {
			MySQLConnection.closeStatement(set);
		}

		commentJSON.put("product", product);
		commentJSON.put("number", array.length());
		commentJSON.put("comments", array);

		return commentJSON.toString();
	}

    /**
     * Add a comment to a product
     * <p>
     *  URI: /products/{product_id}/comment
     * </p>
     * @param product : Id of a product to search.
     * @param user : Username of the user who posted the comment
     * @param token : Token string of the user who posted the comment
     * @param title : Title of the comment.
     * @param text : Body of the comment.
     * @param mark : The rate this user gave to this product.
     * @return The result of this search as specified in API.
     */
	@Path("/{product}/comments")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String addProductComment(
			@PathParam("product") int product,
            @FormParam("user") String user,
            @FormParam("token") String token,
            @FormParam("title") String title,
            @FormParam("text") String text,
            @FormParam("mark") int mark
	){
		JSONObject error = new JSONObject();
		error.put("error", 0);

		if(StringUtils.isValid(user) && StringUtils.isValid(token) &&
                StringUtils.isValid(title) && StringUtils.isValid(text)){
            if(UserUtils.validateUserToken(user, token)){
                int userId = UserIdCache.getUserId(user);

                if(!RestApp.getSql().runAsyncUpdate(MySQLQueries.INSERT_COMMENT,
                        new SQLParameterString(title), new SQLParameterString(text),
                        new SQLParameterInteger(mark), new SQLParameterInteger(userId),
                        new SQLParameterInteger(product))){
                    error.put("error", 3);
                }
            }else{
                error.put("error", 2);
            }

        }else{
            error.put("error", 1);
        }

        return error.toString();
	}

    /**
     * Search a specific comment of a product in database
     * <p>
     *  URI: /products/{product_id}/comments/{comment_id}
     * </p>
     * @param product : Id of a product to search.
     * @param comment_id : Id of a comment to search.
     * @return The result of this search as specified in API.
     */
	@Path("/{product}/comments/{comment_id}")
	@GET
	public String getProductComment(
			@PathParam("product") int product,
			@PathParam("comment_id") int comment_id
	){
		JSONObject obj = new JSONObject();
		JSONObject commentJSON = new JSONObject(defaultProductCommentJSON, JSONObject.getNames(defaultProductCommentJSON));

		ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_PRODUCT_COMMENT, new SQLParameterInteger(product), new SQLParameterInteger(comment_id));
		try {
			if(set.first()){
				commentJSON.put("id", set.getInt("id"));
				commentJSON.put("product", set.getInt("product_id"));
				commentJSON.put("user", set.getString("nick"));
				commentJSON.put("title", set.getString("title"));
				commentJSON.put("text", set.getString("opinion_text"));
				commentJSON.put("rate", set.getDouble("product_mark"));
			}
		} catch (SQLException e) {
			obj.put("error", 1);
			e.printStackTrace();
		}finally {
			MySQLConnection.closeStatement(set);
		}

		obj.put("comment", commentJSON);

		return obj.toString();
	}

    /**
     * Search the prices of a product for all the vendors who sell it.
     * <p>
     *  URI: /products/{product_id}/vendors[?n={amount}]
     * </p>
     * @param product : Id of a product to search.
     * @param products : Amount of products to search.
     * @return The result of this search as specified in API.
     */
	@Path("/{product_id}/vendors")
	@GET
	public String getProductVendors(
			@PathParam("product_id") int product,
			@DefaultValue("" + PRODUCT_DEFAULT_VENDOR_NUMBER) @QueryParam("n") int products
	){
		JSONObject obj = new JSONObject(defaultProductVendorJSON, JSONObject.getNames(defaultProductVendorJSON));
		JSONArray vendors = new JSONArray();

		ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_VENDOR_PRICES_FOR_PRODUCT, new SQLParameterInteger(product), new SQLParameterInteger(products));

		try {
			while(set.next()){
				JSONObject price = new JSONObject();
				JSONObject vendorData = new JSONObject(defaultVendorJSON, JSONObject.getNames(defaultVendorJSON));

				vendorData.put("id", set.getInt("id"));
				vendorData.put("name", set.getString("vendor_name"));
				vendorData.put("url", set.getString("url"));

				price.put("vendor", vendorData);
				price.put("price", set.getDouble("price"));
				vendors.put(price);
			}
		} catch (SQLException e) {
			obj.put("error", 1);
			e.printStackTrace();
		}finally {
			MySQLConnection.closeStatement(set);
		}


		obj.put("product", product);
		obj.put("number", vendors.length());
		obj.put("vendors", vendors);
		return obj.toString();

	}

	static {
		defaultProductJSON = new JSONObject();
		defaultProductJSON.put("id", -1);
		defaultProductJSON.put("name", "");
		defaultProductJSON.put("public_rate", -1.0);
		defaultProductJSON.put("description", "");
		defaultProductJSON.put("category", new JSONArray());
		defaultProductJSON.put("tags", new JSONArray());


		defaultProductCommentsJSON = new JSONObject();
		defaultProductCommentsJSON.put("product", -1);
		defaultProductCommentsJSON.put("number", 0);
		defaultProductCommentsJSON.put("comments", new JSONArray());

		defaultProductCommentJSON = new JSONObject();
		defaultProductCommentJSON.put("id", -1);
		defaultProductCommentJSON.put("product", -1);
		defaultProductCommentJSON.put("user", "");
		defaultProductCommentJSON.put("title", "");
		defaultProductCommentJSON.put("text", "");
		defaultProductCommentJSON.put("rate", -1.0);

		defaultProductVendorJSON = new JSONObject();
		defaultProductVendorJSON.put("product", -1);
		defaultProductVendorJSON.put("number", 0);
		defaultProductVendorJSON.put("vendors", new JSONArray());

		defaultVendorJSON = new JSONObject();
		defaultVendorJSON.put("id", -1);
		defaultVendorJSON.put("name", "");
		defaultVendorJSON.put("url", "");
		defaultVendorJSON.put("rate", -1.0);

	}

}
