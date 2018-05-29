package es.eina.sql.entities;

import es.eina.RestApp;
import es.eina.cache.AlbumCache;
import es.eina.cache.UserFollowersCache;
import es.eina.crypt.Crypter;
import es.eina.utils.StringUtils;
import es.eina.utils.UserUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.NaturalId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.transaction.Transactional;
import java.sql.Date;
import java.util.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    @Column(name="twitter", nullable = false)
    private String twitter = "";

    @Column(name="facebook", nullable = false)
    private String facebook = "";

    @Column(name="instagram", nullable = false)
    private String instagram = "";

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    private EntityToken token;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private EntityUserValues userValues;


    @ManyToMany(mappedBy = "followers")
    private Set<EntitySongList> following = new LinkedHashSet<>();


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    //@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE})
    //@Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<EntityAlbum> albums = new HashSet<>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(
        name = "user_liked_songs",
        joinColumns = { @JoinColumn(name = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "song_id")}
    )
    Set<EntitySong> songsLiked = new HashSet<>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(
            name = "user_faved_songs",
            joinColumns = { @JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "song_id")}
    )
    Set<EntitySong> songsFaved = new HashSet<>();


    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    //@Cascade(org.hibernate.annotations.CascadeType.ALL)
    Set<EntityUserSongData> songsListened = new HashSet<>();

    //This user has this followers
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy = "followee")
    //@Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<EntityUserFollowers> followers = new HashSet<>();

    //This user follows this users
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy = "follower")
    private Set<EntityUserFollowers> followees = new HashSet<>();

    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntityUser(){

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
    }

    public void updateToken(){
        if(this.token != null){
            this.token.updateToken();
        }else{
            this.token = new EntityToken(this);
        }
    }

    public void verifyAccount(){
        if(userValues == null){
            userValues = new EntityUserValues(this);
        }

        userValues.setVerified(true);
    }

    public int unverifyAccount(){
        if(userValues == null) return -2;

        userValues.setVerified(false);
        if(userValues.cleanUp()){
            userValues = null;
            return 0;
        }
        return 0;
    }

    public void makeAdmin(){
        if(userValues == null){
            userValues = new EntityUserValues(this);
        }

        userValues.setAdmin(true);
    }

    public int demoteAdmin(){
        if(userValues == null) return -2;

        userValues.setAdmin(false);
        if(userValues.cleanUp()){
            userValues = null;
            return 0;
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
        }else if("twitter".equals(key)){
            if(value instanceof String){
                twitter = (String) value;
                code = 0;
            }
        }else if("facebook".equals(key)){
            if(value instanceof String){
                facebook = (String) value;
                code = 0;
            }
        }else if("instagram".equals(key)){
            if(value instanceof String){
                instagram = (String) value;
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
                this.birthDate = new Date(((Number) value).longValue());
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

        return code;
    }

    public int deleteToken(Session s){
        if(this.token != null) {
            token.removeSession();
            if(token.shouldRemoveToken()) {
                s.delete(token);
                this.token = null;
            }
            return 0;
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

    public boolean isVerified() {
        return userValues != null && userValues.isVerified();
    }

    public String getTwitter() {
        return twitter;
    }

    public String getFacebook() {
        return facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    /*public Set<EntitySong> getSongs() {
        return songs;
    }*/

    public JSONArray getUserSongs() {
        JSONArray songs = new JSONArray();
        if(this.albums != null) {
            for (EntityAlbum album : this.albums) {
                JSONArray albumSongs = album.getSongsAsArray();
                for (int i = 0; i < albumSongs.length(); i++) {
                    songs.put(albumSongs.get(i));
                }
            }
        }
        return songs;
    }

    public boolean isSongFaved(EntitySong song){
        return this.songsFaved.contains(song);
    }

    public boolean favSong(EntitySong song){ return this.songsFaved.add(song); }

    public boolean unfavSong(EntitySong song){ return this.songsFaved.remove(song);}

    public boolean listenSong(EntitySong song){
        EntityUserSongData entity = new EntityUserSongData(this, song);
        return this.songsListened.add(entity);
    }

    public boolean addAlbum(EntityAlbum entityAlbum) {
        return this.albums.add(entityAlbum);
    }

    public JSONArray getUserAlbums() {
        JSONArray albums = new JSONArray();
        if(this.albums != null) {
            for (EntityAlbum album : this.albums) {
                albums.put(album.getAlbumId());
            }
        }
        return albums;
    }

    /**
     * Returns a list of users who are following this user
     */
    public Set<EntityUser> getFollowers(){
        Set<EntityUser> user = new HashSet<>();
        for(EntityUserFollowers followee : followers){
            user.add(followee.getFollower());
        }
        return user;
    }

    /**
     * Returns a list of users this user follows
     */
    public Set<EntityUser> getFollowees(){
        Set<EntityUser> user = new HashSet<>();
        for(EntityUserFollowers followers : followees){
            user.add(followers.getFollowee());
        }
        return user;
    }

    private void addFollowee(EntityUser usr, EntityUserFollowers data){
        this.followers.add(data);
    }

    private void removeFollowee(EntityUserFollowers usr){
        this.followers.remove(usr);
    }

    public boolean followUser(EntityUser other){
        if(!getFollowers().contains(other)){
            EntityUserFollowers obj = new EntityUserFollowers(this, other);
            other.addFollowee(this, obj);
            this.followees.add(obj);

            return true;
        }

        return false;
    }

    public boolean unFollowUser(Session s, EntityUser other){
        if(getFollowers().contains(other)){
            EntityUserFollowers obj = UserFollowersCache.getFollower(s, other.getId(), getId());
            other.removeFollowee(obj);
            this.followees.remove(obj);
            return true;
        }

        return false;
    }

    public JSONArray getFavedSongs() {
        JSONArray array = new JSONArray();
        for (EntitySong song: songsFaved) {
            array.put(song.getId());
        }
        return array;
    }

    public void removeFollowers(Session s) {
        for(EntityUserFollowers user : followees){
            s.delete(user);
        }

        for(EntityUserFollowers user : followers){
            s.delete(user);
        }
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
