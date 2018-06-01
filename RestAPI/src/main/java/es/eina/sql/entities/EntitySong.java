package es.eina.sql.entities;

import es.eina.geolocalization.Geolocalizer;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ColumnDefault;
import org.json.JSONObject;

import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity(name = "song")
@Table(name = "songs")
public class EntitySong extends EntityBase {

    public static final JSONObject defaultSongJSON;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "country", length = 3, nullable = false)
    private String country;

    @Column(name = "genre", nullable = false)
    @ColumnDefault("-1")
    private int genre;

    @Column(name = "upload_time", nullable = false)
    private long uploadTime;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private EntityAlbum album;

    @ManyToMany(mappedBy = "songs", cascade = CascadeType.ALL)
    private Set<EntitySongList> lists = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "songsFaved", cascade = CascadeType.ALL)
    private Set<EntityUser> usersFavers = new HashSet<>();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<EntityUserSongData> usersListeners = new HashSet<>();

    private EnumSongGenre enumGenre;


    /**
     * DO NOT use this method as it can only be used by Hibernate
     */
    public EntitySong() {
    }

    public EntitySong(EntityAlbum album, String title, String country, String genreStr) {
        this.album = album;
        this.title = title;
        this.country = country;
        this.uploadTime = System.currentTimeMillis();
        setGenre(genreStr);
    }

    private void setGenre(String genreStr) {

        this.genre = -1;
        for(EnumSongGenre enumGenre : EnumSongGenre.values()){
            if(enumGenre.toString().equals(genreStr)){
                this.genre = enumGenre.getIndex();
                this.enumGenre = enumGenre;
            }
        }
        if(genre < 0 || genre >= EnumSongGenre.values().length) {
            this.enumGenre = null;
            this.genre = -1;
        }
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return album != null ? album.getUserId() : -1;
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

    public boolean isSongFaved(EntityUser user) {
        return this.usersFavers.contains(user);
    }

    public boolean favSong(EntityUser user) {
        return this.usersFavers.add(user);
    }

    public boolean unfavSong(EntityUser user) {
        return this.usersFavers.remove(user);
    }

    public Set<EntityUserSongData> getListeners() {
        return this.usersListeners;
    }

    public boolean setAlbum(EntityAlbum album) {
        boolean b = false;
        if (this.album == null) {
            this.album = album;
            album.updateAlbum();
            b = true;
        } else if (album == null) {
            this.album.removeSong(this);
            this.album = null;
            b = true;
        }

        return b;
    }

    public boolean removeFromAlbum() {
        if (this.album != null) {
            album.updateAlbum();
            this.album = null;
            return true;
        }

        return false;
    }

    public EntityAlbum getAlbum() {
        return album;
    }

    public JSONObject toJSON() {
        JSONObject songJSON = new JSONObject(defaultSongJSON, JSONObject.getNames(defaultSongJSON));

        songJSON.put("id", getId());
        songJSON.put("user_id", getUserId());
        songJSON.put("album_id", album != null ? album.getAlbumId() : null);
        songJSON.put("title", getTitle());
        songJSON.put("country", getCountry());
        songJSON.put("upload_time", getUploadTime());

        return songJSON;
    }

    static {
        defaultSongJSON = new JSONObject();
        defaultSongJSON.put("id", -1L);
        defaultSongJSON.put("user_id", -1L);
        defaultSongJSON.put("title", "");
        defaultSongJSON.put("country", Geolocalizer.DEFAULT_COUNTRY);
        defaultSongJSON.put("upload_time", -1L);
    }

    public void setTime(long l) {
        this.uploadTime = l;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public enum EnumSongGenre{

        ROCK, POP, PUNK, FOLK;

        private int index;

        EnumSongGenre(){
            this.index = -1;
        }

        private void setIndex(int i){
            this.index = i;
        }

        public int getIndex() {
            return index;
        }
    }

    static {
        int i = 0;
        for(EnumSongGenre genre : EnumSongGenre.values()){
            genre.setIndex(i);
            i++;
        }
    }
}
