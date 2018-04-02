package es.eina.sql.entities;

import javax.persistence.*;

@Entity(name="userData")
@Table(name="user_values")
public class EntityUserValues extends EntityBase{

    @Id
    @Column(name = "user_id")
    private long userId;

    @Column(name = "admin")
    private boolean admin;

    @OneToOne
    @JoinColumn(name = "user_id")
    private EntityUser user;

    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntityUserValues(){
        update();
    }

    public EntityUserValues(EntityUser user){
        this(user, false);
    }

    public EntityUserValues(EntityUser user, boolean admin){
        this.user = user;
        this.userId = user.getId();
        this.admin = admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAdmin() {
        return admin;
    }

    public EntityUser getUser() {
        return user;
    }
}
