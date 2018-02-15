package es.eina.search;

import es.eina.RestApp;
import es.eina.sql.MySQLQueries;
import org.apache.commons.lang3.Validate;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class IndexPrice extends Index {

    public static final String VENDOR_INDEX_COLUMN = "vendor";
    public static final String PRODUCT_ID_INDEX_COLUMN = "p_id";
    private static final String PRICE_INDEX_COLUMN = "price";

    @Override
    protected void reloadIndex() {
        closeIndexWriter();

//SELECT * FROM product;
        ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_PRODUCT_PRICES);
        constructWriter();

        try {
            while (set.next()) {
                try {
                    int pId = set.getInt("id_product");
                    String vendor_name = set.getString("vendor_name");
                    double price = set.getFloat("price");

                    createCurrentDocument();
                    storeInDocument(PRODUCT_ID_INDEX_COLUMN, pId);
                    storeInDocument(VENDOR_INDEX_COLUMN, vendor_name);
                    storeInDocument(PRICE_INDEX_COLUMN, price);

                    addCurrentDocumentToIndex();

                } catch (SQLException e) {
                    System.out.println("Error fetching products.");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching products.");
            e.printStackTrace();
        }

        //printAllDocumentsInIndex();

        commitDataToIndex();
    }

    @Override
    public List<ScoreDoc> search(String query, float minPrice, float maxPrice, int searchAmount, Object extraData) {
        Validate.isTrue(extraData instanceof List, "extraData is not a List");
        try {
            IndexSearcher searcher = new IndexSearcher(getReader());
            BooleanQuery products = new BooleanQuery();
            List<Integer> data = (List<Integer>) extraData;
            for (Integer term: data) {
                products.add(new TermQuery(new Term(PRODUCT_ID_INDEX_COLUMN, String.valueOf(term))), BooleanClause.Occur.SHOULD);
            }
            NumericRangeQuery priceQuery = NumericRangeQuery.newFloatRange(PRICE_INDEX_COLUMN, minPrice, maxPrice, true, true);

            BooleanQuery finalQuery = new BooleanQuery();
            finalQuery.add(products, BooleanClause.Occur.MUST);
            finalQuery.add(priceQuery, BooleanClause.Occur.MUST);

            TopDocs result = searcher.search(finalQuery, searchAmount);
            ScoreDoc[] hits = result.scoreDocs;
            return Arrays.asList(hits);
        } catch (IOException e) {
            System.out.println("An error occured while trying to perform query: " + query);
            e.printStackTrace();
        }

        return null;
    }
}
