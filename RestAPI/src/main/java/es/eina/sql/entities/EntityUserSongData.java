package es.eina.sql.entities;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "user_listened_songs")
@Entity(name = "listenedSong")
@IdClass(EntityUserSongData.UserSongId.class)
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

    @Embeddable
    public static class UserSongId implements Serializable {

        private long time;

        private long author;

        private long song;

        public UserSongId(){}

        public UserSongId(EntityUser user, EntitySong song, long time){
            this.song = song.getId();
            this.author = user.getId();
            this.time = time;
        }
    }

}
