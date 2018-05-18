package es.eina.cache;

import es.eina.sql.entities.EntityToken;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.entities.EntityUserValues;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCache {

    private static Map<Long, EntityUser> users = new ConcurrentHashMap<>();

    private static final Map<String, Long> nameToId = new ConcurrentHashMap<>();
    private static final Map<Long, String> idToName = new ConcurrentHashMap<>();

    /**
     * Removes from cache all entities that has been stored first and have expired.
     * A value is considered expired if isEntityValid() returns false (it is the entity has benn stored "x" ms before).
     *
     * @param time : Current epoch time in ms.
     */
    public static void cleanUp(long time) {
        List<EntityUser> remove = new ArrayList<>();

        for (EntityUser data : users.values()) {
            if (!data.isEntityValid(time)) {
                remove.add(data);
            }
        }

        saveEntities(remove);
    }

    public static void forceSave(){
        saveEntities(users.values());
    }

    private static void saveEntities(Collection<EntityUser> remove){
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            for (EntityUser data : remove) {
                System.err.println("Check " + data.getNick());
                if(data.isDirty() || data.getToken().isDirty() || data.getUserValues().isDirty()) {
                    System.err.println(data.getNick() + " is dirty, updating");
                    try {
                        tr = session.beginTransaction();
                        session.saveOrUpdate(data);
                        EntityToken token = data.getToken();
                        EntityUserValues userValues = data.getUserValues();

                        if(token != null && token.isDirty()) session.saveOrUpdate(token);
                        if(userValues != null && userValues.isDirty()) session.saveOrUpdate(userValues);

                        tr.commit();
                        users.remove(data.getId());
                        nameToId.remove(data.getNick());
                        idToName.remove(data.getId());
                    } catch (Exception e) {
                        if (tr != null) {
                            tr.rollback();
                        }
                    }
                }
            }
        }
    }


    public static void addUser(EntityUser user) {
        long id = user.getId();
        if (!users.containsKey(id)) {
            String nick = user.getNick();
            users.put(id, user);
            nameToId.put(nick, id);
            idToName.put(id, nick);
        }
    }

    private static EntityUser loadUser(String nick) {
        EntityUser user = null;
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            user = session.byNaturalId(EntityUser.class)
                    .using("nick", nick)
                    .load();
            tr.commit();
            addUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        }

        return user;
    }

    private static EntityUser loadUser(long userId) {
        EntityUser user = null;
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            user = session.load(EntityUser.class, userId);
            tr.commit();
            addUser(user);
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
        }
        return user;
    }

    public static EntityUser getUser(long userId) {
        EntityUser user = users.get(userId);
        if (user == null) {
            user = loadUser(userId);
        }

        return user;
    }

    public static EntityUser getUser(String nick) {
        Long userId = nameToId.get(nick);
        EntityUser user = userId != null ? users.get(userId) : null;
        if (user == null) {
            user = loadUser(nick);
        }

        return user;
    }

    public static boolean updateUser(EntityUser user){
        getUser(user.getId());
        if (users.containsKey(user.getId())){
            users.replace(user.getId(),user);
            return true;
        }
        return false;
    }

    public static boolean deleteUser(EntityUser user){
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            String nick = user.getNick();
            long id = user.getId();
            tr = session.beginTransaction();
            session.delete(user);
            tr.commit();
            users.remove(id);
            nameToId.remove(nick);
            idToName.remove(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        }

        return false;
    }

    public static long getId(String nick){
        return nameToId.get(nick);
    }

    public static String getNick(long id){
        return idToName.get(id);
    }

}
