package es.eina.sql.entities;

import javax.persistence.*;

@Table(name = "user_listened_songs")
@Entity(name = "listenedSong")
public class EntityUserSongData extends EntityBase {

    @Id
    @Column(name="time")
    private long time;

    @Id
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="user_id")
    private EntityUser author;

    @Id
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="song_id")
    private EntitySong song;

    public EntityUserSongData(){
        update();
    }

    public EntityUserSongData(EntityUser user, EntitySong song){
        this.song = song;
        this.author = user;
        this.time = System.currentTimeMillis();
        update();
    }

}
