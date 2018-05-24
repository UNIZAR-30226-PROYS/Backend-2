package es.eina.sql.entities;

import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.*;

@Entity(name="album")
@Table(name="albums")
public class EntityAlbum extends EntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "user_id",nullable = false)
    private long userId;

    @Column(name = "title",nullable = false)
    private String title;
    
    @Column(name = "publish_year",nullable = false)
    private int publishYear;

    @Column(name = "update_time",nullable = false)
    private long updateTime;

    @Column(name = "upload_time",nullable = false)
    private long uploadTime;
    
    @Column(name = "image",nullable = false)
    private String image;	//es un URI de una imagen

    @OneToMany(mappedBy = "album", cascade=CascadeType.ALL)
    private Set<EntitySong> songs = new HashSet<>();


    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private EntityUser user;


    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntityAlbum(){update();}

    public EntityAlbum(EntityUser user, String title, int year, String image) {

        this.userId = user.getId();
        this.title = title;
        this.publishYear = year;
        this.image = image;
        this.updateTime = System.currentTimeMillis();
        this.uploadTime = updateTime;

        update();
    }

    public void updateAlbum(){
        this.updateTime = System.currentTimeMillis();
        update();
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

    public long getUpdateTime() {
        return updateTime;
    }
    
    public String getImage() {
        return image;
    }
    
    public Set<EntitySong> getSongs() {
    	return songs;
    }
    
    public JSONArray getSongsAsArray() {
    	JSONArray arr = new JSONArray();
    	for (EntitySong song: songs) {
    	    arr.put(song.getId());
    	}
    	return arr;
    }

    @Override
    @Transactional
    public void save() {
        Session s = HibernateUtils.getSessionFactory().getCurrentSession();
        s.saveOrUpdate(this.songs);
    }

}