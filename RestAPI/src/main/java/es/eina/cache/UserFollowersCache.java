package es.eina.cache;

import es.eina.sql.entities.EntityUserFollowers;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;


public class UserFollowersCache {

    public static boolean addFollower(Session s, EntityUserFollowers entity) {
        return HibernateUtils.addEntityToDB(s, entity);
    }

    public static EntityUserFollowers getFollower(Session s, long followee, long follower) {
        return HibernateUtils.getEntity(s, EntityUserFollowers.class, new EntityUserFollowers.FollowerPrimaryKey(followee, follower));
    }

    public static boolean deleteFollower(Session s, EntityUserFollowers ent) {
        return HibernateUtils.deleteFromDB(s, ent);
    }

}
