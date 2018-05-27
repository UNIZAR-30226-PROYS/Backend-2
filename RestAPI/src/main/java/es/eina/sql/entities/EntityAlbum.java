package es.eina.sql.entities;

import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cascade;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.*;

@Entity(name="album")
@Table(name="albums")
public class EntityAlbum extends EntityBase {

    public static final JSONObject defaultAlbumJSON;

    @Id
    @GeneratedValue
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "title",nullable = false)
    private String title;
    
    @Column(name = "publish_year",nullable = false)
    private int publishYear;

    @Column(name = "update_time",nullable = false)
    private long updateTime;

    @Column(name = "upload_time",nullable = false)
    private long uploadTime;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EntitySong> songs = new HashSet<>();


    @ManyToOne
    @JoinColumn(name = "user_id")
    private EntityUser user;


    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntityAlbum(){}

    public EntityAlbum(EntityUser user, String title, int year) {
        this.user = user;
        this.title = title;
        this.publishYear = year;
        this.updateTime = System.currentTimeMillis();
        this.uploadTime = updateTime;
    }

    public void updateAlbum(){
        this.updateTime = System.currentTimeMillis();
    }
    
    public long getAlbumId() {
        return id;
    }

    public long getUserId() {
        return user.getId();
    }

    public String getTitle() {
        return title;
    }
    
    public int getPublishYear() {
        return publishYear;
    }

    public long getUpdateTime() {
        return updateTime;
    }
    
    public Set<EntitySong> getSongs() {
    	return songs;
    }

    public JSONArray getSongsAsArray() {
    	JSONArray arr = new JSONArray();
            for (EntitySong song : songs) {
                arr.put(song.getId());
            }
    	return arr;
    }

    public void removeSong(EntitySong entitySong) {
        this.songs.remove(entitySong);
    }

    public boolean addSong(EntitySong entitySong) {
        return this.songs.add(entitySong);
    }

    public JSONObject toJSON(){
        JSONObject albumJSON = new JSONObject(defaultAlbumJSON, JSONObject.getNames(defaultAlbumJSON));
        albumJSON.put("id", id);
        albumJSON.put("user_id", user.getId());
        albumJSON.put("title", title);
        albumJSON.put("publish_year", publishYear);
        albumJSON.put("update_time", updateTime);
        albumJSON.put("songs", getSongsAsArray());

        return albumJSON;
    }

    static {
        defaultAlbumJSON = new JSONObject();
        defaultAlbumJSON.put("id", -1L);
        defaultAlbumJSON.put("user_id", -1L);
        defaultAlbumJSON.put("title", "");
        defaultAlbumJSON.put("publish_year", -1);
        defaultAlbumJSON.put("update_time", -1L);
        defaultAlbumJSON.put("image", "");
    }
}