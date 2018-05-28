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

    public static boolean addUser(Session s, EntityUser user) {
        return HibernateUtils.addEntityToDB(s, user);
    }

    public static EntityUser getUser(Session s, long userID) {
        return HibernateUtils.getEntity(s, EntityUser.class, userID);
    }

    public static boolean deleteUser(Session s, EntityUser user) {
        return HibernateUtils.deleteFromDB(s, user);
    }

    public static EntityUser getUser(Session s, String nick) {
        return HibernateUtils.getEntityByAttribute(s, EntityUser.class, "nick", nick);
    }
}
