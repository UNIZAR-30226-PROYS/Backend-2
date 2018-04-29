CREATE TABLE public.sessions
(
    user_id int PRIMARY KEY NOT NULL,
    token VARCHAR(32) NOT NULL,
    time BIGINT NOT NULL,
    CONSTRAINT sessions_users_id_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);