package es.eina.search;

import es.eina.sql.entities.EntitySong;
import es.eina.sql.utils.HibernateUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import org.hibernate.Session;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IndexSongs extends Index {

    public static final String ID_INDEX_COLUMN = "id";
    public static final String AUTHOR_ID_INDEX_COLUMN = "author_id";
    public static final String TITLE_INDEX_COLUMN = "title";
    public static final String COUNTRY_INDEX_COLUMN = "country";
    public static final String UPLOAD_TIME_INDEX_COLUMN = "upload_time";

    private static final QueryParser queryParserTitle = new QueryParser(Version.LUCENE_40, TITLE_INDEX_COLUMN, indexAnalyzer);
    //private static final QueryParser queryParserGenre = new QueryParser(Version.LUCENE_40, , indexAnalyzer);
    private static final QueryParser queryParserCountry = new QueryParser(Version.LUCENE_40, COUNTRY_INDEX_COLUMN, indexAnalyzer);

    private String genre;
    private String country;
    private long minTime, maxTime;

    @Override
    protected void reloadIndex() {


        List<EntitySong> songs;
        try(Session s = HibernateUtils.getSessionFactory().openSession()){
            songs = s.createQuery("from songs", EntitySong.class).list();
        }

        if (getWriter() != null) {
            closeIndexWriter();
        }
        constructWriter();

        if(songs != null){
            for (EntitySong song: songs) {
                createCurrentDocument();
                storeInDocument(ID_INDEX_COLUMN, song.getId());
                storeInDocument(TITLE_INDEX_COLUMN, song.getTitle());
                storeInDocument(AUTHOR_ID_INDEX_COLUMN, song.getUserId());
                storeInDocument(COUNTRY_INDEX_COLUMN, song.getCountry());
                storeInDocument(UPLOAD_TIME_INDEX_COLUMN, song.getUploadTime());
                addCurrentDocumentToIndex();
            }
        }

        commitDataToIndex();
    }

    public void setSearchParams(String genre, String country, long minTime, long maxTime){
        this.genre = genre;
        this.country = country;
        this.minTime = minTime;
        this.maxTime = maxTime;
    }

    @Override
    public List<ScoreDoc> search(String query, int searchAmount) {
        try {
            IndexSearcher searcher = new IndexSearcher(getReader());

            NumericRangeQuery uploadTimeQuery = NumericRangeQuery.newLongRange(UPLOAD_TIME_INDEX_COLUMN, minTime, maxTime, true, true);
            Query queryParsedTitle = queryParserTitle.parse(query);

            BooleanQuery finalQuery = new BooleanQuery();
            finalQuery.add(queryParsedTitle, BooleanClause.Occur.MUST);
            finalQuery.add(uploadTimeQuery, BooleanClause.Occur.MUST);

            if(country != null && !country.isEmpty()){
                Query queryParsedCountry = queryParserCountry.parse(country);
                finalQuery.add(queryParsedCountry, BooleanClause.Occur.MUST);
            }

            TopDocs result = searcher.search(finalQuery, searchAmount);
            return Arrays.asList(result.scoreDocs);
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
