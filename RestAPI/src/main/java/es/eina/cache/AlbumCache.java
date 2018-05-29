package es.eina.cache;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AlbumCache {

    public static boolean addAlbum(Session s, EntityAlbum album) {
        return HibernateUtils.addEntityToDB(s, album);
    }

    public static EntityAlbum getAlbum(Session s, long albumID) {
        return HibernateUtils.getEntity(s, EntityAlbum.class, albumID);
    }

    public static boolean deleteAlbum(Session session, EntityAlbum ent) {
        return HibernateUtils.deleteFromDB(session, ent);
    }

}
