package es.eina.cache;

import es.eina.sql.entities.EntityAlbum;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.entities.EntityUserFollowers;
import es.eina.sql.utils.HibernateUtils;


public class UserFollowersCache {

    public static boolean addFollower(EntityUserFollowers entity) {
        return HibernateUtils.addEntityToDB(entity);
    }

    public static EntityUserFollowers getFollower(EntityUser follower, EntityUser followee) {
        return HibernateUtils.getEntity(EntityUserFollowers.class, new EntityUserFollowers.FollowerPrimaryKey(follower, followee));
    }

    public static boolean deleteFollower(EntityUserFollowers entity) {
        return HibernateUtils.deleteFromDB(entity);
    }

}
