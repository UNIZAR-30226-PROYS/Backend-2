package es.eina.sql.utils;

import es.eina.sql.entities.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HibernateUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateUtils.class);

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;
    private static Session session;

    public static SessionFactory configureDatabase(InputStream f) {
        if (sessionFactory == null) {
            LOG.debug("Reading " + f);
            try {
                Properties login = new Properties();
                login.load(f);

                LOG.debug("Connecting to " + login.getProperty("url"));

                StandardServiceRegistryBuilder registryBuilder =
                        new StandardServiceRegistryBuilder();

                Map<String, Object> settings = new HashMap<>();
                settings.put(Environment.DRIVER, login.getProperty("driver"));
                settings.put(Environment.URL, login.getProperty("url"));
                settings.put(Environment.USER, login.getProperty("user"));
                settings.put(Environment.PASS, login.getProperty("pass"));
                settings.put(Environment.HBM2DDL_AUTO, "update");
                settings.put(Environment.SHOW_SQL, true);
                //settings.put(Environment.USE_SQL_COMMENTS, true);

                //settings.put(Environment.FORMAT_SQL, true);
                settings.put("hibernate.current_session_context_class", "org.hibernate.context.internal.ThreadLocalSessionContext");
                String createDrop = login.getProperty("create-drop");
                if("true".equals(createDrop)){
                    LOG.debug("Enable CREATE-DROP property!!!! Beware, this MUST be a debug/test environment.");
                    settings.put(Environment.HBM2DDL_AUTO, "create-drop");
                }

                // HikariCP settings

                // Maximum waiting time for a connection from the pool
                settings.put("hibernate.hikari.connectionTimeout", "20000");
                // Minimum number of ideal connections in the pool
                settings.put("hibernate.hikari.minimumIdle", "10");
                // Maximum number of actual connection in the pool
                settings.put("hibernate.hikari.maximumPoolSize", "20");
                // Maximum time that a connection is allowed to sit ideal in the pool
                settings.put("hibernate.hikari.idleTimeout", "300000");

                settings.put("log4j.logger.org.hibernate", "INFO, hb");
                settings.put("log4j.logger.org.hibernate.SQL", "DEBUG");
                settings.put("log4j.logger.org.hibernate.type", "TRACE");
                settings.put("log4j.logger.org.hibernate.hql.ast.AST", "info");
                settings.put("log4j.logger.org.hibernate.tool.hbm2ddl", "warn");
                settings.put("log4j.logger.org.hibernate.hql", "debug");
                settings.put("log4j.logger.org.hibernate.cache", "info");
                settings.put("log4j.logger.org.hibernate.jdbc", "debug");

                settings.put("log4j.appender.hb", "org.apache.log4j.ConsoleAppender");
                settings.put("log4j.appender.hb.layout", "org.apache.log4j.PatternLayout");
                settings.put("log4j.appender.hb.layout.ConversionPattern", "HibernateLog --> %d{HH:mm:ss} %-5p %c - %m%n");
                settings.put("log4j.appender.hb.Threshold", "TRACE");

                registryBuilder.applySettings(settings);

                registry = registryBuilder.build();
                MetadataSources sources = new MetadataSources(registry);

                sources.addAnnotatedClass(EntityUser.class);
                sources.addAnnotatedClass(EntityToken.class);
                sources.addAnnotatedClass(EntityUserValues.class);
                sources.addAnnotatedClass(EntitySong.class);
                sources.addAnnotatedClass(EntityAlbum.class);
                sources.addAnnotatedClass(EntityUserSongData.class);
                sources.addAnnotatedClass(EntityUserFollowers.class);

                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();
                session = sessionFactory.openSession();

            } catch (Exception e) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    public static Session getSession(){
        //return getSessionFactory().openSession();
        if (session == null) {
            throw new RuntimeException("Cannot access a non-built Session.");
        }
        return session;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new RuntimeException("Cannot access a non-built SessionFactory.");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            sessionFactory.close();
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public static boolean addEntityToDB(EntityBase entity){
        Session session = HibernateUtils.getSession();
        Transaction tr = session.beginTransaction();
        try {
            session.saveOrUpdate(entity);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }

            LOG.debug("Cannot add Entity to DB", e);
        }

        return false;
    }

    public static boolean deleteFromDB(EntityBase entity){
        Session session = HibernateUtils.getSession();
        Transaction tr = session.beginTransaction();
        try {
            session.delete(entity);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            e.printStackTrace();
            LOG.debug("Cannot delete Entity from DB", e);
        }

        return false;
    }

    public static <T extends EntityBase> T getEntity(Class<T> clazz, Serializable key){
        T entity = null;
        Transaction tr = null;
        Session session = HibernateUtils.getSession();
        try{
            tr = session.beginTransaction();
            entity = session.get(clazz, key);
            tr.commit();
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
            LOG.debug("Cannot load Entity from DB", e);
        }
        return entity;
    }

    public static <T extends EntityBase> T getEntityByAttribute(Class<T> clazz, String attribute, Serializable key){
        T entity = null;
        Transaction tr = null;
        Session session = HibernateUtils.getSession();
        try{
            tr = session.beginTransaction();
            entity = session.byNaturalId(clazz)
                    .using(attribute, key)
                    .load();
            tr.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        }

        return entity;
    }

}