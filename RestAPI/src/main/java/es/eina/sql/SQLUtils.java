package es.eina.sql;

import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;

public class SQLUtils {

    public static long getRowCount(String table, String where){
        long rowCount;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            rowCount = (Long) session.createQuery("SELECT COUNT(e) as c FROM " + table + " e WHERE " + where).iterate().next();
        } catch (Exception e) {
            e.printStackTrace();
            rowCount = -1;
        }

        return rowCount;
    }
}
