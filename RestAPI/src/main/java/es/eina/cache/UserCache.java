package es.eina.cache;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityToken;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.entities.EntityUserValues;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCache {

    private static final Logger LOG = LoggerFactory.getLogger(UserCache.class);

    public static boolean addUser(EntityUser user) {
        return HibernateUtils.addEntityToDB(user);
    }

    public static EntityUser getUser(long userID) {
        return HibernateUtils.getEntity(EntityUser.class, userID);
    }

    public static EntityUser getUser(String nick) {
        return HibernateUtils.getEntityByAttribute(EntityUser.class, "nick", nick);
    }

    public static boolean deleteUser(EntityUser ent) {
        boolean b = false;
        try(Session session = HibernateUtils.getSession()) {
            Transaction tr = session.beginTransaction();
            try {
                EntityUser user = session.get(EntityUser.class, ent.getId());
                session.delete(user);
                tr.commit();
                b = true;
            } catch (Exception e) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                e.printStackTrace();
                LOG.debug("Cannot delete Entity from DB", e);
            }
        }

        return b;
        //return HibernateUtils.deleteFromDB(user);
    }

}
