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

    @ManyToMany
    @JoinTable(
            name = "song_list_songs",
            joinColumns = { @JoinColumn(name = "list_id")},
            inverseJoinColumns = {@JoinColumn(name = "song_id")}
    )
    private Set<EntitySong> songs = new HashSet<>();

    @ManyToMany(mappedBy = "following")
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

    public String getTitle() {
        return title;
    }

    public EntityUser getAuthor() {
        return author;
    }

    public Long getCreationTime() {
        return creationTime;
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
