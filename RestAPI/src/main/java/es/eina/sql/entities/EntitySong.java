package es.eina.sql.entities;

import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name="song")
@Table(name="songs")
public class EntitySong extends EntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id",nullable = false)
    private Long id;

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

    @Deprecated
    @Column(name = "likes")
    private long likes;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "album_id")
    private EntityAlbum album;

    @ManyToMany(mappedBy = "songsLiked", cascade = CascadeType.ALL)
    private Set<EntityUser> usersLikers = new HashSet<>();

    @ManyToMany(mappedBy = "songsFaved", cascade = CascadeType.ALL)
    private Set<EntityUser> usersFavers = new HashSet<>();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<EntityUserSongData> usersListeners = new HashSet<>();

    @ManyToMany (mappedBy = "song_list")
    private Set<EntitySongList> lists = new HashSet<>();


    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntitySong(){}

    public EntitySong(EntityAlbum album, String title, String country) {
        this.album = album;
        this.title = title;
        this.country = country;
        this.uploadTime = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return album != null ? album.getUserId() : -1;
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

    public boolean isSongFaved(EntityUser user){
        return this.usersFavers.contains(user);
    }

    public boolean favSong(EntityUser user){ return this.usersFavers.add(user); }

    public boolean unfavSong(EntityUser user){ return this.usersFavers.remove(user); }

    public Set<EntityUserSongData> getListeners(){
        return this.usersListeners;
    }

    public boolean setAlbum(EntityAlbum album){
        boolean b = false;
            if (this.album == null) {
                this.album = album;
                album.updateAlbum();
                b = true;
            } else if (album == null) {
                this.album.removeSong(this);
                this.album = null;
                b = true;
            }

        return b;
    }

    public boolean removeFromAlbum(){
        if(this.album != null) {
            album.updateAlbum();
            this.album = null;
            return true;
        }

        return false;
    }

}
