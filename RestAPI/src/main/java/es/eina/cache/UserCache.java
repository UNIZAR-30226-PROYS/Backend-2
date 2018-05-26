package es.eina.cache;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityToken;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.entities.EntityUserValues;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCache {

    public static boolean addUser(EntityUser user) {
        return HibernateUtils.addEntityToDB(user);
    }

    public static EntityUser getUser(long userID) {
        return HibernateUtils.getEntity(EntityUser.class, userID);
    }

    public static EntityUser getUser(String nick) {
        return HibernateUtils.getEntityByAttribute(EntityUser.class, "nick", nick);
    }

    public static boolean deleteUser(EntityUser user) {
        return HibernateUtils.deleteFromDB(user);
    }

}
