package es.eina.sql;

import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigInteger;

public class SQLUtils {

    public static long getRowCount(String table, String where){
        long rowCount;
        try(Session session = HibernateUtils.getSession()) {
            Transaction t = session.beginTransaction();
            try {
                rowCount = (Long) session.createQuery("SELECT COUNT(e) as c FROM " + table + " e WHERE " + where)
                        .iterate()
                        .next();
                t.commit();
            } catch (Exception e) {
                t.rollback();
                e.printStackTrace();
                rowCount = -1;
            }
        }

        return rowCount;
    }

    public static long getRowCountSQL(String table, String where){
        long rowCount;
        try(Session session = HibernateUtils.getSession()) {
            Transaction t = session.beginTransaction();
            try {
                rowCount = ((BigInteger) session.createSQLQuery("SELECT COUNT(e) as c FROM " + table + " e WHERE " + where).uniqueResult()).longValue();
                t.commit();
            } catch (Exception e) {
                t.rollback();
                e.printStackTrace();
                rowCount = -1;
            }
        }

        return rowCount;

    }
}
