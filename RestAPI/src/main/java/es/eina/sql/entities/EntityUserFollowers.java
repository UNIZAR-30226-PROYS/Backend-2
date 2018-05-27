package es.eina.sql.entities;

import javax.persistence.*;

@Entity(name="followers")
@Table(name="user_followers")
public class EntityUserFollowers {

    @Id
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "follower")
    private EntityUser follower;

    @Id
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "followee")
    private EntityUser followee;

    @Column(name = "time")
    private long time;

    public EntityUserFollowers() {}

    public EntityUserFollowers(EntityUser user, EntityUser follows) {
        this.follower = user;
        this.followee = follows;
        this.time = System.currentTimeMillis();
    }

    public EntityUser getFollowee() {
        return followee;
    }

    public EntityUser getFollower() {
        return follower;
    }

    public long getTime() {
        return time;
    }
}
