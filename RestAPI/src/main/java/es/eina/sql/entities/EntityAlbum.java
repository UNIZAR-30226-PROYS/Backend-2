package es.eina.sql.entities;

import javax.persistence.*;
import java.util.*;

@Entity(name="album")
@Table(name="albums")
public class EntityAlbum extends EntityBase {

    @Id
    @Column(name = "album_id",nullable = false)
    private long AlbumId;

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
    private LinkedList<EntitySong> songs;


    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntityAlbum(){update();}

    public EntityAlbum(long AlbumId, long userId, String title, int year, String image, EntitySong song) {
    	
        this.AlbumId = AlbumId;
        this.userId = userId;
        this.title = title;
        this.publishYear = year;
        this.image = image;
        this.songs.add(song);
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
        return AlbumId;
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
    
    public LinkedList<EntitySong> getSongs() {
    	return songs;
    }
    
    public String getSongStrings() {
    	int limit = songs.size(); 
    	String s = "";
    	for (int i = 0; i < limit; i++) {
    		EntitySong song = songs.get(i);
    		s = s + Long.toString(song.getId()) + " , ";
    	}
    	return s;
    }

}