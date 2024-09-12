CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE SEQUENCE role_seq START WITH 1 INCREMENT BY 1;

INSERT INTO roles (id, name)
VALUES (NEXTVAL('role_seq'), 'ADMIN');
INSERT INTO roles (id, name)
VALUES (NEXTVAL('role_seq'), 'USER');

CREATE TABLE users
(
    id        BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL
);

CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE;

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE;

CREATE TABLE attachments
(
    id         BIGSERIAL PRIMARY KEY,
    file_name  TEXT                                NOT NULL,
    file_link  TEXT                                NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE SEQUENCE attachment_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE tasks
(
    id            BIGSERIAL PRIMARY KEY,
    description   TEXT                                NOT NULL,
    attachment_id BIGINT,
    complete      BOOLEAN   DEFAULT FALSE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_id       BIGINT                              NOT NULL
);

CREATE SEQUENCE task_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_attachment
        FOREIGN KEY (attachment_id)
            REFERENCES attachments (id);

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE;
