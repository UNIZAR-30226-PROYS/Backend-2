package es.eina.sql.entities;

import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @ManyToOne
    @JoinColumn(name = "user_id", insertable=false, updatable=false)
    private EntityUser user;

    @ManyToMany (mappedBy = "songs", cascade = CascadeType.ALL)
    private Set<EntitySongList> lists = new LinkedHashSet<>();



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

    public Set<EntitySongList> getLists() {
        return lists;
    }

    public void setLists(Set<EntitySongList> lists) {
        this.lists = lists;
    }
}
