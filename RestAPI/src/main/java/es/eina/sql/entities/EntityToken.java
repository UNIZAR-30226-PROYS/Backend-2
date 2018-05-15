package es.eina.sql.entities;

import es.eina.utils.RandomString;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity(name="token")
@Table(name="sessions")
public class EntityToken extends EntityBase{

    private static final RandomString randomTokenGenerator = new RandomString(16);
    private static final long TOKEN_VALID_TIME = 2592000000L;

    @Id
    @Column(name = "user_id")
    private long user_id;

    @Column(name = "token")
    private String token;

    @Column(name = "time")
    private long time;

    @OneToOne
    @JoinColumn(name = "user_id")
    private EntityUser user;

    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntityToken(){
        update();
    }

    public EntityToken(EntityUser user) {
        this.user_id = user.getId();
        this.user = user;
        updateToken();
    }

    public EntityUser getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public long getTime() {
        return time;
    }

    void updateToken() {
        this.token = randomTokenGenerator.nextString();
        this.time = System.currentTimeMillis() + TOKEN_VALID_TIME;
        update();
    }

    public boolean isValid(String token) {
        return time >= System.currentTimeMillis() && this.token.equals(token);
    }

    @Override
    public long getId() {
        return 0;
    }
}
