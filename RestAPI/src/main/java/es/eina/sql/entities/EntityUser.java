package es.eina.sql.entities;

import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.persistence.Entity;
import java.sql.Date;

@Entity(name="user")
@Table(name="users")
public class EntityUser extends EntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "nick", nullable = false, unique = true)
    private String nick;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name="mail", nullable = false)
    private String mail;

    @Column(name="pass", nullable = false)
    private String pass;

    @Column(name="birth_date", nullable = false)
    private Date birthDate;

    @Column(name="bio", nullable = false)
    private String bio;

    @Column(name="country", nullable = false)
    private String country;

    @Column(name="register_date", nullable = false)
    private long register_date;

    @OneToOne(mappedBy = "user")
    private EntityToken token;

    @OneToOne(mappedBy = "user")
    private EntityUserValues userValues;

    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntityUser(){
        update();
    }

    public EntityUser(String nick, String username, String mail, String pass, Date birthDate, String bio, String country) {
        this.nick = nick;
        this.username = username;
        this.mail = mail;
        this.pass = pass;
        this.birthDate = birthDate;
        this.bio = bio;
        this.country = country;
        this.register_date = System.currentTimeMillis();

        update();
    }

    public void updateToken(){
        if(this.token != null){
            this.token.updateToken();
        }else{
            this.token = new EntityToken(this);
        }
        update();
    }

    public void verifyAccount(){
        if(userValues == null){
            userValues = new EntityUserValues(this);
        }

        userValues.setVerified(true);
        update();
    }

    public int unverifyAccount(){
        if(userValues == null) return -2;

        userValues.setVerified(false);
        if(userValues.cleanUp()){
            int code = userValues.deleteEntity();
            if(code == 0) {
                userValues = null;
                update();
            }
            return code;
        }
        return 0;
    }

    public int deleteToken(){
        if(this.token != null) {
            int code = token.deleteEntity();

            if(code == 0) {
                this.token = null;
                update();
            }
            return code;
        }
        return -2;
    }

    public Long getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    public String getUsername() {
        return username;
    }

    public String getMail() {
        return mail;
    }

    public String getPass() {
        return pass;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getBio() {
        return bio;
    }

    public String getCountry() {
        return country;
    }

    public long getRegisterDate() {
        return register_date;
    }

    public EntityToken getToken() {
        return token;
    }

    public EntityUserValues getUserValues() {
        return userValues;
    }

    public boolean isAdmin() {
        return userValues != null && userValues.isAdmin();
    }
}
