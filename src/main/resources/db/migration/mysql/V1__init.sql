-- V1__init.sql (MySQL 8+)
-- Baseline schema. Mirrors the entity model as of Phase 5 completion.
-- Uses MySQL-native AUTO_INCREMENT and LONGTEXT for comment content.
-- InnoDB is explicit for foreign-key support.

CREATE TABLE users (
    id             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(255),
    email          VARCHAR(255),
    password       VARCHAR(255),
    bio            VARCHAR(255),
    profile_image  VARCHAR(255),
    role           ENUM('ROLE_ADMIN', 'ROLE_USER') NOT NULL
) ENGINE=InnoDB;

CREATE TABLE posts (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255),
    content     TEXT,
    created_at  TIMESTAMP(6) NULL,
    user_id     BIGINT,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE tag (
    id    BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE post_tags (
    post_id  BIGINT NOT NULL,
    tag_id   BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_tags_tag  FOREIGN KEY (tag_id)  REFERENCES tag   (id)
) ENGINE=InnoDB;

CREATE TABLE comments (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content     LONGTEXT,
    created_at  TIMESTAMP(6) NULL,
    post_id     BIGINT,
    user_id     BIGINT,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;
