package es.eina.sql.entities;

import javax.persistence.*;
import java.util.*;

@Entity(name="album")
@Table(name="albums")
public class EntityAlbum extends EntityBase {

    @Id
    @Column(name = "id",nullable = false)
    private long id;

    @Column(name = "user_id",nullable = false)
    private long userId;

    @Column(name = "title",nullable = false)
    private String title;
    
    @Column(name = "publish_year",nullable = false)
    private int publishYear;

    @Column(name = "upload_time",nullable = false)
    private long uploadTime;
    
    @Column(name = "image",nullable = false)
    private String image;	//es un URI de una imagen

    @OneToMany(mappedBy = "album")
    @JoinColumn(name = "id", insertable=false, updatable=false)
    Set<EntitySong> songs = new HashSet<>();


    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntityAlbum(){update();}

    public EntityAlbum(EntityUser user, String title, int year, String image) {

        this.userId = user.getId();
        this.title = title;
        this.publishYear = year;
        this.image = image;
        this.uploadTime = System.currentTimeMillis();

        update();
    }

    /**
     * Add song to album.
     *
     * @param song : Song to add.
     * 
     * @return True if song successfully added, False otherwise.
     */
    public boolean addSong(EntitySong song) {
    	boolean OK;
    	OK = this.songs.add(song);
    	if(OK) {
    		this.uploadTime = System.currentTimeMillis();

        	update();
    	}
    		
    	return OK;
    }

    /**
     * Remove song from album.
     *
     * @param song : Song to remove.
     * 
     * @return True if song successfully removed, False otherwise.
     */
    public boolean removeSong(EntitySong song) {
    	boolean OK;
    	OK = this.songs.remove(song);
    	if(OK) {
    		this.uploadTime = System.currentTimeMillis();

        	update();
    	}
    		
    	return OK;
    }
    
    public long getAlbumId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }
    
    public int getPublishYear() {
        return publishYear;
    }

    public long getUploadTime() {
        return uploadTime;
    }
    
    public String getImage() {
        return image;
    }
    
    public Set<EntitySong> getSongs() {
    	return songs;
    }
    
    public String getSongStrings() {
    	String s = "";
    	for (EntitySong song: songs) {
    		s = s + Long.toString(song.getId()) + " , ";
    	}
    	return s;
    }

}