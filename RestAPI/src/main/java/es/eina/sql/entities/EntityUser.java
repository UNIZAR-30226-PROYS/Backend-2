package es.eina.sql.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.sql.Date;

@Entity(name="user")
@Table(name="users")
public class EntityUser {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "nick")
    private String nick;

    @Column(name = "username")
    private String username;

    @Column(name="mail")
    private String mail;

    @Column(name="pass")
    private String pass;

    @Column(name="birth_date")
    private Date birthDate;

    @Column(name="bio")
    private String bio;

    @Column(name="country")
    private String country;

    @Column(name="register_date")
    private long register_date;

    @OneToOne(mappedBy = "user")
    private EntityToken token;

    public EntityUser(String nick, String username, String mail, String pass, Date birthDate, String bio, String country) {
        this.nick = nick;
        this.username = username;
        this.mail = mail;
        this.pass = pass;
        this.birthDate = birthDate;
        this.bio = bio;
        this.country = country;
        this.register_date = System.currentTimeMillis();
    }

    public void setToken(EntityToken token){
        this.token = token;
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

    public long getRegister_date() {
        return register_date;
    }

    public EntityToken getToken() {
        return token;
    }
}
