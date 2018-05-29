package es.eina.sql.entities;

import org.json.JSONObject;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity(name="song_list")
@Table(name="user_song_lists")
public class EntitySongList extends EntityBase{

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "creation_time",nullable = false)
    private Long creationTime;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "author_id")
    private EntityUser author;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "song_list_songs",
            joinColumns = { @JoinColumn(name = "list_id")},
            inverseJoinColumns = {@JoinColumn(name = "song_id")}
    )
    private Set<EntitySong> songs = new HashSet<>();

    @ManyToMany (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "song_list_user_follows",
            joinColumns = { @JoinColumn(name = "song_list_id", nullable = false, referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")}
    )
    private Set<EntityUser> followers = new LinkedHashSet<>();

    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntitySongList(){}

    public EntitySongList(String title, EntityUser user) {
        this.title = title;
        this.author = user;
        this.creationTime = System.currentTimeMillis();
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

    public Set<EntitySong> getSongs() {
        return songs;
    }

    public void setSongs(Set<EntitySong> songs) {
        this.songs = songs;
    }

    public boolean addSong(EntitySong song){
        return this.songs.add(song);
    }
    public boolean removeSong(EntitySong song){
        return this.songs.remove(song);
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("creation_time", creationTime);
        obj.put("author", author.getId());
        obj.put("amount", songs.size());

        return obj;
    }


    public Set<EntityUser> getFollowed() {
        return followers;
    }

    public void setFollowed(Set<EntityUser> followedby) {
        this.followers = followedby;
    }
    public boolean addfollower(EntityUser user){
        return this.followers.add(user);
    }
    public boolean removefollower(EntityUser user){
        return this.followers.remove(user);
    }

}
