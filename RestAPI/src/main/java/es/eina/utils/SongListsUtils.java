package es.eina.utils;

import es.eina.cache.SongCache;
import es.eina.cache.SongListCache;
import es.eina.cache.UserCache;
import es.eina.crypt.Crypter;
import es.eina.sql.SQLUtils;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntitySongList;
import es.eina.sql.entities.EntityUser;
import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.sql.Date;

public class SongListsUtils {

    /**
     * Add a new nick in the database.
     * @return Null if the user couldn't be added, the actual user if it could be added.
     */
    @Transactional
    public static @Nullable
	EntitySongList addList(Session s, String title, EntityUser user) {
        //(nick, username, mail, pass, birth_date, bio, country, register_date)
		EntitySongList list = new EntitySongList(title, user);
        return SongListCache.addSongList(s, list) ? list : null;
    }
}
