package es.eina.sql.entities;

import javax.persistence.*;

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
}
