package es.eina.cache;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AlbumCache {

    public static boolean addAlbum(EntityAlbum album) {
        return HibernateUtils.addEntityToDB(album);
    }

    public static EntityAlbum getAlbum(long albumID) {
        return HibernateUtils.getEntity(EntityAlbum.class, albumID);
    }

    public static boolean deleteAlbum(EntityAlbum album) {
        return HibernateUtils.deleteFromDB(album);
    }

}
