package es.eina.sql.entities;

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
            System.err.println("Updating token for user " + nick);
            this.token.updateToken();
        }else{
            System.err.println("Creating new token for user " + nick);
            this.token = new EntityToken(this);
        }
        update();
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
}
