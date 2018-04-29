package es.eina.cache;

import es.eina.sql.entities.EntityBase;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class BaseCache<T extends EntityBase> {

    private static Map<EnumCache, BaseCache> caches = new ConcurrentHashMap<>();
    private static Logger logger = Logger.getLogger("CacheContainer");

    private Map<Long, T> entities = new ConcurrentHashMap<>();

    private final Class<T> clazz;

    public BaseCache(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Removes from cache all entities that has been stored first and have expired.
     * A value is considered expired if isEntityValid() returns false (it is the entity has benn stored "x" ms before).
     *
     * @param time : Current epoch time in ms.
     */
    public void cleanUp(long time) {
        List<T> remove = new ArrayList<>();

        for (T data : entities.values()) {
            if (!data.isEntityValid(time)) {
                remove.add(data);
            }
        }

        saveEntities(remove);
    }

    public void forceSave() {
        saveEntities(entities.values());
    }

    private void saveEntities(Collection<T> remove) {
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            for (T entity : remove) {
                if (entity.isDirty()) {
                    try {
                        tr = session.beginTransaction();
                        onPreSave(session, entity);
                        session.saveOrUpdate(entity);
                        onPostSave(session, entity);
                        tr.commit();
                        entities.remove(entity.getId());
                    } catch (Exception e) {
                        if (tr != null) {
                            tr.rollback();
                        }
                    }
                }
            }
        }
    }


    public void addSongList(T list) {
        long id = list.getId();
        if (!entities.containsKey(id)) {
            entities.put(id, list);
        }
    }

    private T loadEntity(long key) {
        T entity = null;
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            onPreLoad(session);
            entity = session.load(clazz, key);
            onPostLoad(session, entity);
            tr.commit();
            addSongList(entity);
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
        }
        return entity;
    }

    public T getEntity(long key) {
        T song = entities.get(key);
        if (song == null) {
            song = loadEntity(key);
        }

        return song;
    }

    public boolean deleteEntity(T entity) {
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            long id = entity.getId();
            tr = session.beginTransaction();
            onPreDelete(session, entity);
            session.delete(entity);
            onPostDelete(session, entity);
            tr.commit();
            entities.remove(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        }

        return false;
    }

    public void onPreDelete(Session session, T entity){}
    public void onPostDelete(Session session, T entity){}
    public void onPreSave(Session session, T entity){}
    public void onPostSave(Session session, T entity){}
    public void onPreLoad(Session session){}
    public void onPostLoad(Session session, T entity){}


    public enum EnumCache {
        USER(EntityUser.class),
        SONG(EntitySong.class),
        SONG_LIST(EntitySongList.class);

        private Class<? extends EntityBase> clazz;

        EnumCache(Class<? extends EntityBase> clazz) {
            this.clazz = clazz;
            registerCache(this);
        }

        public Class<? extends EntityBase> getClazz() {
            return clazz;
        }
    }

    @SuppressWarnings("unchecked")
    public static <Q extends EntityBase> BaseCache<Q> getCache(EnumCache cache){
        return caches.get(cache);
    }

    private static void registerCache(EnumCache type){
        BaseCache cache = null;
        try {
            cache = BaseCache.class.getConstructor(Class.class).newInstance(type.getClazz());
        } catch (InstantiationException e) {
            logger.severe("Cannot instantiate cache of type " + type.getClazz());
        } catch (IllegalAccessException e) {
            logger.severe("Cannot access class " + type.getClazz());
        } catch (InvocationTargetException e) {
            logger.severe("Cannot invocate cache of type " + type.getClazz());
        } catch (NoSuchMethodException e) {
            logger.severe("Cache does not have a valid constructor " + type.getClazz());
        }

        if(cache != null){
            caches.put(type, cache);
        }
    }
    
}
