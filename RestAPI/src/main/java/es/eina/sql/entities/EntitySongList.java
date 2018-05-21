package es.eina.sql.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name="song_list")
@Table(name="user_song_lists")
public class EntitySongList extends EntityBase{

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "author_id",nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "creation_time",nullable = false)
    private Long creationTime;


    @ManyToOne
    @JoinColumn(name = "author_id", insertable=false, updatable=false)
    private EntityUser author;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "song_list_songs",
            joinColumns = { @JoinColumn(name = "list_id")},
            inverseJoinColumns = {@JoinColumn(name = "song_id")}
    )
    private Set<EntitySong> songs = new HashSet<>();

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
            name = "song_list_user_follows",
            joinColumns = { @JoinColumn(name = "song_list_id", nullable = false, referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")}
    )
    private Set<EntityUser> followers;

    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntitySongList(){update();}

    public EntitySongList(String title, EntityUser user) {
        this.userId = user.getId();
        this.title = title;
        this.author = user;
        this.creationTime = System.currentTimeMillis();

        update();
    }

    public long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public EntityUser getAuthor() {
        return author;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public Set<EntitySong> getSongs() {
        return songs;
    }

    public void setSongs(Set<EntitySong> songs) {
        this.songs = songs;
    }

    public void addSong(EntitySong song){
        this.songs.add(song);
    }
    public void removeSong(EntitySong song){
        this.songs.remove(song);
    }


    public Set<EntityUser> getFollowed() {
        return followers;
    }

    public void setFollowed(Set<EntityUser> followedby) {
        this.followers = followedby;
    }
    public void addfollower(EntityUser user){
        this.followers.add(user);
    }
    public void removefollower(EntityUser user){
        this.followers.remove(user);
    }

}
