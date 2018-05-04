package es.eina.search;

import es.eina.RestApp;
import es.eina.sql.MySQLQueries;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class IndexProduct extends Index {

    private static final QueryParser queryParserName = new QueryParser(Version.LUCENE_40, NAME_INDEX_COLUMN, indexAnalyzer);
    private static final QueryParser queryParserDesc = new QueryParser(Version.LUCENE_40, DESC_INDEX_COLUMN, indexAnalyzer);

    protected void reloadIndex() {
        if (getWriter() != null) {
            closeIndexWriter();
        }

//SELECT * FROM product;
        ResultSet set = RestApp.getSql().runAsyncQuery(MySQLQueries.GET_PRODUCTS);
        constructWriter();

        try {
            while (set.next()) {
                try {
                    int id = set.getInt("id");
                    String product_name = set.getString("product_name");
                    String description = set.getString("description");
                    float sum = set.getFloat("valuation_sum");
                    float rate;

                    if (sum != 0) {
                        rate = set.getFloat("valuation_amount") / sum;
                    } else {
                        rate = 0.0f;
                    }

                    System.out.println("Storing "+product_name+" (" + id + ") in index.");
                    createCurrentDocument();
                    storeInDocument(ID_INDEX_COLUMN, id);
                    storeInDocument(NAME_INDEX_COLUMN, product_name);
                    storeInDocument(DESC_INDEX_COLUMN, description);
                    storeInDocument(RATE_INDEX_COLUMN, rate);

                    addCurrentDocumentToIndex();

                } catch (SQLException e) {
                    System.out.println("Error fetching products.");
                    e.printStackTrace();
                }
            }

            printAllDocumentsInIndex();
        } catch (SQLException e) {
            System.out.println("Error fetching products.");
            e.printStackTrace();
        }

        //printAllDocumentsInIndex();

        commitDataToIndex();
    }

    public List<ScoreDoc> search(String query, int searchAmount) {
        try {
            IndexSearcher searcher = new IndexSearcher(getReader());
            Query queryName = queryParserName.parse(query);
            Query queryDesc = queryParserDesc.parse(query);

            BooleanQuery productQuery = new BooleanQuery();
            productQuery.add(queryName, BooleanClause.Occur.SHOULD);
            productQuery.add(queryDesc, BooleanClause.Occur.SHOULD);

            TopDocs result = searcher.search(productQuery, searchAmount);
            ScoreDoc[] hits = result.scoreDocs;
            return Arrays.asList(hits);
        } catch (IOException e) {
            System.out.println("An error occured while trying to perform query: " + query);
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Cannot parse query: " + query);
            e.printStackTrace();
        }

        return null;
    }

}
