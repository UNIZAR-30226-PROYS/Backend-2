
CREATE TABLE public.users
(
    id SERIAL PRIMARY KEY NOT NULL,
    nick VARCHAR(32) NOT NULL,
    username VARCHAR(64) DEFAULT '' NOT NULL,
    mail VARCHAR(128) NOT NULL,
    pass VARCHAR(128) NOT NULL,
    birth_date BIGINT NOT NULL,
    bio VARCHAR(1024) DEFAULT '' NOT NULL,
    country VARCHAR(3) DEFAULT 'O1' NOT NULL,
    register_date BIGINT
);
CREATE UNIQUE INDEX users_nick_uindex ON public.users (nick);
CREATE UNIQUE INDEX users_id_uindex ON public.users (id);
COMMENT ON TABLE public.users IS 'User table';



CREATE TABLE public.songs
(
    id SERIAL PRIMARY KEY NOT NULL,
    "user" INT NOT NULL,
    title VARCHAR(128) NOT NULL,
    country VARCHAR(3) NOT NULL,
    upload_time BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT songs_users_id_fk FOREIGN KEY ("user") REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX songs_title_index ON public.songs (title);
CREATE INDEX songs_country_index ON public.songs (country);
COMMENT ON TABLE public.songs IS 'Song table';

---

CREATE TABLE public.followers
(
    user1 INT,
    user2 INT,
    CONSTRAINT followers_user1_user2_pk PRIMARY KEY (user1, user2)
);

CREATE TABLE public.song_reproductions
(
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    song_id INT NOT NULL,
    time BIGINT NOT NULL,
    CONSTRAINT song_reproductions_users_id_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT song_reproductions_songs_id_fk FOREIGN KEY (song_id) REFERENCES songs (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE public.song_likes
(
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    song_id INT NOT NULL,
    time BIGINT NOT NULL,
    CONSTRAINT song_reproductions_users_id_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT song_reproductions_songs_id_fk FOREIGN KEY (song_id) REFERENCES songs (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TYPE SOCIAL_NETWORK AS ENUM ('TW', 'IG', 'FB');

CREATE TABLE public.user_social_networks
(
    user_id INT NOT NULL,
    network SOCIAL_NETWORK NOT NULL,
    value VARCHAR(128),
    CONSTRAINT user_social_networks_user_id_network_pk PRIMARY KEY (user_id, network),
    CONSTRAINT user_social_networks_users_id_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE public.user_values
(
    user_id INT PRIMARY KEY,
    admin BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT user_values_users_id_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
COMMENT ON TABLE public.user_values IS 'Mark which users are admins';