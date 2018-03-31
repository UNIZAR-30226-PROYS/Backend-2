package es.eina.search;

import org.apache.lucene.search.ScoreDoc;

import java.io.File;
import java.util.List;

public class IndexPriceAndProduct extends Index {

    private IndexPrice priceIndex;
    private IndexProduct productIndex;

    public IndexPriceAndProduct(File basePath) {
        priceIndex = new IndexPrice();
        productIndex = new IndexProduct();

        //priceIndex.openIndex(new File(basePath, "price").getAbsolutePath());
        //productIndex.openIndex(new File(basePath, "product").getAbsolutePath());
    }

    @Override
    protected void reloadIndex() {
        priceIndex.reloadIndex();
        productIndex.reloadIndex();
    }

    @Override
    public List<ScoreDoc> search(String query, float minPrice, float maxPrice, int searchAmount, Object extraData) {
        List<ScoreDoc> scorePrice = priceIndex.search(query, minPrice, maxPrice, searchAmount, null);

        //Map<Integer, Product> data = new HashMap<>();
        for (ScoreDoc score : scorePrice) {

        }

        List<ScoreDoc> scoreProduct = productIndex.search(query, minPrice, maxPrice, searchAmount, null);
        return null;
    }
}
