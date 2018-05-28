package es.eina.sql;

import org.hibernate.Session;

import java.math.BigInteger;

public class SQLUtils {

    public static long getRowCount(Session s, String table, String where) {
        long rowCount;
        try {
            rowCount = (Long) s.createQuery("SELECT COUNT(e) as c FROM " + table + " e WHERE " + where)
                    .iterate()
                    .next();
        } catch (Exception e) {
            e.printStackTrace();
            rowCount = -1;
        }

        return rowCount;
    }

    public static long getRowCountSQL(Session s, String table, String where) {
        long rowCount;
        try {
            rowCount = ((BigInteger) s.createSQLQuery("SELECT COUNT(e) as c FROM " + table + " e WHERE " + where).uniqueResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            rowCount = -1;
        }

        return rowCount;

    }
}
