package es.eina.sql;

import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SQLUtils {

    public static long getRowCount(String table, String where){
        long rowCount;
        Session session = HibernateUtils.getSession();
        Transaction t = session.beginTransaction();
        try{
            rowCount = (Long) session.createQuery("SELECT COUNT(e) as c FROM " + table + " e WHERE " + where).iterate().next();
            t.commit();
        } catch (Exception e) {
            t.rollback();
            e.printStackTrace();
            rowCount = -1;
        }

        return rowCount;
    }

    public static long getRowCountSQL(String table, String where){
        long rowCount;
        Session session = HibernateUtils.getSession();
        Transaction t = session.beginTransaction();
        try{
            rowCount = session.createSQLQuery("SELECT COUNT(e) as c FROM " + table + " e WHERE " + where).list().size();
            t.commit();
        } catch (Exception e) {
            t.rollback();
            e.printStackTrace();
            rowCount = -1;
        }

        return rowCount;
    }
}
