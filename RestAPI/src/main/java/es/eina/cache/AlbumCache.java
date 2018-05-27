package es.eina.cache;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AlbumCache {

    private static final Logger LOG = LoggerFactory.getLogger(AlbumCache.class);

    public static boolean addAlbum(EntityAlbum album) {
        return HibernateUtils.addEntityToDB(album);
    }

    public static EntityAlbum getAlbum(long albumID) {
        return HibernateUtils.getEntity(EntityAlbum.class, albumID);
    }

    public static boolean deleteAlbum(EntityAlbum ent) {
        boolean b = false;
        try(Session session = HibernateUtils.getSession()) {
            Transaction tr = session.beginTransaction();
            try {
                EntityAlbum album = session.get(EntityAlbum.class, ent.getAlbumId());
                session.delete(album);
                tr.commit();
                b = true;
            } catch (Exception e) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                e.printStackTrace();
                LOG.debug("Cannot delete Album from DB", e);
            }
        }

        return b;
        //return HibernateUtils.deleteFromDB(album);
    }

}
