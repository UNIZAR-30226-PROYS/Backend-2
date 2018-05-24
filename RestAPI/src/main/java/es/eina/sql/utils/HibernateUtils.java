package es.eina.sql.utils;

import es.eina.RestApp;
import es.eina.sql.entities.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.beanvalidation.HibernateTraversableResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class HibernateUtils {

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    public static SessionFactory configureDatabase(InputStream f) {
        if (sessionFactory == null) {
            Logger.getLogger("HibernateUtils").info("Reading " + f);
            try {
                Properties login = new Properties();
                login.load(f);

                Logger.getLogger("HibernateUtils").info("Connecting to " + login.getProperty("url"));

                StandardServiceRegistryBuilder registryBuilder =
                        new StandardServiceRegistryBuilder();

                Map<String, Object> settings = new HashMap<>();
                settings.put(Environment.DRIVER, login.getProperty("driver"));
                settings.put(Environment.URL, login.getProperty("url"));
                settings.put(Environment.USER, login.getProperty("user"));
                settings.put(Environment.PASS, login.getProperty("pass"));
                settings.put(Environment.HBM2DDL_AUTO, "update");
                settings.put(Environment.SHOW_SQL, true);
                settings.put("hibernate.current_session_context_class", "org.hibernate.context.internal.ThreadLocalSessionContext");

                // HikariCP settings

                // Maximum waiting time for a connection from the pool
                settings.put("hibernate.hikari.connectionTimeout", "20000");
                // Minimum number of ideal connections in the pool
                settings.put("hibernate.hikari.minimumIdle", "10");
                // Maximum number of actual connection in the pool
                settings.put("hibernate.hikari.maximumPoolSize", "20");
                // Maximum time that a connection is allowed to sit ideal in the pool
                settings.put("hibernate.hikari.idleTimeout", "300000");

                registryBuilder.applySettings(settings);

                registry = registryBuilder.build();
                MetadataSources sources = new MetadataSources(registry);

                sources.addAnnotatedClass(EntityUser.class);
                sources.addAnnotatedClass(EntityToken.class);
                sources.addAnnotatedClass(EntityUserValues.class);
                sources.addAnnotatedClass(EntitySong.class);
                sources.addAnnotatedClass(EntityAlbum.class);
                sources.addAnnotatedClass(EntityUserListenSong.class);

                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();
                sessionFactory.openSession();
            } catch (Exception e) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new RuntimeException("Cannot access a non-built SessionFactory.");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

}