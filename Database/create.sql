
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