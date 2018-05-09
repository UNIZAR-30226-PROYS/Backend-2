package es.eina.utils;

import es.eina.cache.SongCache;
import es.eina.cache.UserCache;
import es.eina.sql.entities.EntitySong;
import es.eina.sql.entities.EntityUser;
import org.json.JSONObject;


public class SongUtils {

    /**
     * Add a new like in the database.
     * @param nick : User's nick.
     * @param token : User's token.
     * @param songID : Song's ID.
     * @return A JSON with response.
     */
    public static String likeSong(String nick, String token, Long songID){
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(token)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(token)) {
                    EntitySong song = SongCache.getSong(songID);
                    if(song != null) {
                        if(!song.isSongLiked(user) && !user.isSongLiked(song)) {
                            if (song.likeSong(user) && user.likeSong(song)) {
                                result.put("error", "ok");
                            } else {
                                result.put("error", "unknownError");
                            }
                        }else{
                            result.put("error", "alreadyLike");
                        }
                    }else{
                        result.put("error", "unknownSong");
                    }
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }

    /**
     * Remove a  like in the database.
     * @param nick : User's nick.
     * @param token : User's token.
     * @param songID : Song's ID.
     * @return A JSON with response.
     */
    public static String unlikeSong(String nick, String token, Long songID){
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(token)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(token)) {
                    EntitySong song = SongCache.getSong(songID);
                    if(song != null) {
                        if(song.isSongLiked(user) && user.isSongLiked(song)) {
                            if (song.unlikeSong(user) && user.unlikeSong(song)) {
                                result.put("error", "ok");
                            } else {
                                result.put("error", "unknownError");
                            }
                        }else{
                            result.put("error", "noLike");
                        }
                    }else{
                        result.put("error", "unknownSong");
                    }
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }

    /**
     * Obtains song's likes in the database.
     * @param songID : Song's ID.
     * @return A JSON with response.
     */
    public static String getLikes(Long songID){
        JSONObject result = new JSONObject();
        EntitySong song = SongCache.getSong(songID);
        if(song != null) {
            long likes = song.getLikes();
            result.put("likes", String.valueOf(likes));
            result.put("error", "ok");
        }else{
            result.put("error", "unknownSong");
        }

        return result.toString();
    }

    /**
     * Add a song to user's fav list.
     * @param nick : User's nick.
     * @param token : User's token.
     * @param songID : Song's ID.
     * @return A JSON with response.
     */
    public static String favSong(String nick, String token, Long songID){
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(token)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(token)) {
                    EntitySong song = SongCache.getSong(songID);
                    if(song != null) {
                        if(!song.isSongFaved(user) && !user.isSongFaved(song)) {
                            if (song.favSong(user) && user.favSong(song)) {
                                result.put("error", "ok");
                            } else {
                                result.put("error", "unknownError");
                            }
                        }else{
                            result.put("error", "alreadyFav");
                        }
                    }else{
                        result.put("error", "unknownSong");
                    }
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }

    /**
     * Remove a song from user's fav list.
     * @param nick : User's nick.
     * @param token : User's token.
     * @param songID : Song's ID.
     * @return A JSON with response.
     */
    public static String unfavSong(String nick, String token, Long songID){
        JSONObject result = new JSONObject();
        if(StringUtils.isValid(nick) && StringUtils.isValid(token)){
            EntityUser user = UserCache.getUser(nick);
            if(user != null){
                if (user.getToken() != null && user.getToken().isValid(token)) {
                    EntitySong song = SongCache.getSong(songID);
                    if(song != null) {
                        if(song.isSongFaved(user) && user.isSongFaved(song)) {
                            if (song.unfavSong(user) && user.unfavSong(song)) {
                                result.put("error", "ok");
                            } else {
                                result.put("error", "unknownError");
                            }
                        }else{
                            result.put("error", "noFav");
                        }
                    }else{
                        result.put("error", "unknownSong");
                    }
                } else {
                    result.put("error", "invalidToken");
                }
            }else{
                result.put("error", "unknownUser");
            }
        }else{
            result.put("error", "invalidArgs");
        }

        return result.toString();
    }


}
