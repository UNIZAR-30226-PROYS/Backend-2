package es.eina.search;

import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import org.hibernate.Session;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IndexUsers extends Index {

    public static final String ID_INDEX_COLUMN = "id";
    public static final String NICK_INDEX_COLUMN = "nick";
    public static final String USERNAME_INDEX_COLUMN = "username";
    public static final String COUNTRY_INDEX_COLUMN = "country";
    public static final String BIO_INDEX_COLUMN = "bio";
    public static final String BIRTH_TIME_INDEX_COLUMN = "birth_time";
    public static final String REGISTER_TIME_INDEX_COLUMN = "register_time";

    private static final QueryParser queryParserNick = new QueryParser(Version.LUCENE_40, NICK_INDEX_COLUMN, indexAnalyzer);
    private static final QueryParser queryParserUsername = new QueryParser(Version.LUCENE_40, USERNAME_INDEX_COLUMN, indexAnalyzer);
    private static final QueryParser queryParserCountry = new QueryParser(Version.LUCENE_40, COUNTRY_INDEX_COLUMN, indexAnalyzer);
    private static final QueryParser queryParserBio = new QueryParser(Version.LUCENE_40, BIO_INDEX_COLUMN, indexAnalyzer);

    private String country;
    private long minRegTime, maxRegTime;
    private long minBirthTime, maxBirthTime;

    @Override
    protected void reloadIndex() {
        if (getWriter() != null) {
            closeIndexWriter();
        }

        List<EntityUser> users;
        try(Session s = HibernateUtils.getSession()){
            users = s.createQuery("from user", EntityUser.class).list();
        }


        constructWriter();

        if(users != null){
            for (EntityUser user: users) {
                createCurrentDocument();
                storeInDocument(ID_INDEX_COLUMN, user.getId());
                storeInDocument(NICK_INDEX_COLUMN, user.getNick());
                storeInDocument(USERNAME_INDEX_COLUMN, user.getUsername());
                storeInDocument(COUNTRY_INDEX_COLUMN, user.getCountry());
                storeInDocument(BIO_INDEX_COLUMN, user.getBio());
                storeInDocument(BIRTH_TIME_INDEX_COLUMN, user.getBirthDate());
                storeInDocument(REGISTER_TIME_INDEX_COLUMN, user.getRegisterDate());
                addCurrentDocumentToIndex();
            }
        }

        commitDataToIndex();
    }

    public void setSearchParams(String country, long minRegTime, long maxRegTime, long minBirthTime, long maxBirthTime){
        this.country = country;
        this.minRegTime = minRegTime;
        this.maxRegTime = maxRegTime;
        this.minBirthTime = minBirthTime;
        this.maxBirthTime = maxBirthTime;
    }

    @Override
    public List<ScoreDoc> search(String query, int searchAmount) {
        try {
            IndexSearcher searcher = new IndexSearcher(getReader());

            NumericRangeQuery regTimeQuery = NumericRangeQuery.newLongRange(REGISTER_TIME_INDEX_COLUMN, minRegTime, maxRegTime, true, true);
            NumericRangeQuery birthTimeQuery = NumericRangeQuery.newLongRange(BIRTH_TIME_INDEX_COLUMN, minBirthTime, maxBirthTime, true, true);
            Query queryParsedNick = queryParserNick.parse(query);
            Query queryParsedUsername = queryParserUsername.parse(query);
            Query queryParsedBio = queryParserBio.parse(query);

            BooleanQuery finalQuery = new BooleanQuery();
            BooleanQuery queryKeyword = new BooleanQuery();
            queryKeyword.add(queryParsedNick, BooleanClause.Occur.SHOULD);
            queryKeyword.add(queryParsedUsername, BooleanClause.Occur.SHOULD);
            queryKeyword.add(queryParsedBio, BooleanClause.Occur.SHOULD);
            finalQuery.add(queryKeyword, BooleanClause.Occur.MUST);
            finalQuery.add(regTimeQuery, BooleanClause.Occur.MUST);
            finalQuery.add(birthTimeQuery, BooleanClause.Occur.MUST);

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

