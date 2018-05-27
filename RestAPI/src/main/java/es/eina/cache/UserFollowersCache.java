package es.eina.cache;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.entities.EntityUserFollowers;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class UserFollowersCache {

    public static boolean addFollower(EntityUserFollowers entity) {
        return HibernateUtils.addEntityToDB(entity);
    }

    public static EntityUserFollowers getFollower(EntityUser follower, EntityUser followee) {
        return HibernateUtils.getEntity(EntityUserFollowers.class, new EntityUserFollowers.FollowerPrimaryKey(follower, followee));
    }

    public static boolean deleteFollower(EntityUserFollowers ent) {
        boolean b = false;
        /*try(Session session = HibernateUtils.getSession()) {
            Transaction tr = session.beginTransaction();
            try {
                EntityUserFollowers album = session.get(EntityUserFollowers.class, ent.getId());
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
        }*/

        return b;
    }

}
