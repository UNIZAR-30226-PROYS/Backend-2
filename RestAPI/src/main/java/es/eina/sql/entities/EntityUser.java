package es.eina.sql.entities;

import es.eina.crypt.Crypter;
import es.eina.utils.StringUtils;
import es.eina.utils.UserUtils;
import org.hibernate.annotations.NaturalId;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import javax.persistence.Entity;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


    @ManyToMany(mappedBy = "followers")
    private Set<EntitySongList> following;

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

    public int updateUser(String key, Object value){
        int code = -1;
        if("username".equals(key)){
            if(value instanceof String){
                username = (String) value;
                code = 0;
            }
        }else if("mail".equals(key)){
            if(value instanceof String){
                mail = (String) value;
                code = 0;
            }
        }else if("bio".equals(key)){
            if(value instanceof String){
                bio = (String) value;
                code = 0;
            }
        }else if("birth_date".equals(key)){
            if(value instanceof Number){
                this.birthDate = new Date((Long) value);
                code = 0;
            }
        }else if("pass".equals(key)){
            if(value instanceof JSONObject){
                JSONObject obj = (JSONObject) value;
                if(obj.has("pass0") && obj.has("pass1") && obj.has("old_pass")) {
                    try {
                        String pass = obj.getString("old_pass");
                        String pass0 = obj.getString("pass0");
                        String pass1 = obj.getString("pass1");
                        if(StringUtils.isValid(pass0) && pass0.equals(pass1) &&
                                UserUtils.checkPassword(this, pass)){
                            this.pass = Crypter.hashPassword(pass0, false);
                            code = 0;
                        }else{
                            code = -2;
                        }
                    }catch (JSONException ignored){}
                }
            }
        }

        if(code == 0) update();
        return code;
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

    public long getId() {
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

    public boolean isVerified() {
        return userValues != null && userValues.isVerified();
    }



    public Set<EntitySongList> getFollowing() {
        return following;
    }

    public void setFollowing(Set<EntitySongList> followedlists) {
        this.following= followedlists;
    }

    public void addfollowing(EntitySongList songlist){
        this.following.add(songlist);
    }
    public void removefollowimg(EntitySongList songlist){
        this.following.remove(songlist);
    }

}
