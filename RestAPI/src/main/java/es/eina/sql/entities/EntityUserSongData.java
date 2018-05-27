package es.eina.sql.entities;

import javax.persistence.*;

@Table(name = "user_listened_songs")
@Entity(name = "listenedSong")
public class EntityUserSongData extends EntityBase {

    @Id
    @Column(name="time")
    private long time;

    @Id
    @ManyToOne
    @JoinColumn(name="user_id")
    private EntityUser author;

    @Id
    @ManyToOne
    @JoinColumn(name="song_id")
    private EntitySong song;

    public EntityUserSongData(){
    }

    public EntityUserSongData(EntityUser user, EntitySong song){
        this.song = song;
        this.author = user;
        this.time = System.currentTimeMillis();
    }

}
