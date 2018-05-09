package es.eina.sql.entities;

import javax.persistence.*;
import java.util.*;

@Entity(name="song")
@Table(name="songs")
public class EntitySong extends EntityBase {

    @Id
    @Column(name = "id",nullable = false)
    private long id;

    @Column(name = "user_id",nullable = false)
    private long userId;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name = "country", length = 3,nullable = false)
    private String country;

    @Column(name = "upload_time",nullable = false)
    private long uploadTime;

    @Column(name = "times_listened", nullable = false)
    private long listened;

    @Column(name = "lyrics")
    private String lyrics;  //ruta a la letra de cancion

    @ManyToOne
    @JoinColumn(name = "user_id", insertable=false, updatable=false)
    private EntityUser user;

    @ManyToOne
    @JoinColumn(name = "album_id", insertable=false, updatable=false)
    private EntityAlbum album;

    @ManyToMany(mappedBy = "user")
    Set<EntityUser> usersLikers = new HashSet<>();

    @ManyToMany(mappedBy = "user")
    Set<EntityUser> usersFavers = new HashSet<>();

//    @ManyToMany(mappedBy = "user")
//    Set<EntityUser> usersListeners = new HashSet<>();

    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntitySong(){update();}


    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getCountry() {
        return country;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public long getListened() {
        return listened;
    }

    public String getLyrics() {
        if (this.lyrics != null) return lyrics;
        else return "No Lyrics";
    }

    public boolean isSongLiked(EntityUser user){ return this.usersLikers.contains(user); }

    public boolean likeSong(EntityUser user){ return this.usersLikers.add(user); }

    public boolean unlikeSong(EntityUser user){ return this.usersLikers.remove(user); }

    public long getLikes(){ return this.usersLikers.size();}

    public boolean isSongFaved(EntityUser user){ return this.usersFavers.contains(user); }

    public boolean favSong(EntityUser user){ return this.usersFavers.add(user); }

    public boolean unfavSong(EntityUser user){ return this.usersFavers.remove(user); }


}
