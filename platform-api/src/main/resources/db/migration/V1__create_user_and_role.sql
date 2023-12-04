CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(256) UNIQUE NOT NULL,
    password VARCHAR(256),
    first_name VARCHAR(256) NOT NULL,
    second_name VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(256) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS user_role (
    role_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (role_id, user_id),
    CONSTRAINT fk_user_role FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_role_user FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

INSERT INTO roles(name) VALUES ('ROLE_ADMIN'), ('ROLE_USER');

INSERT INTO users(email, password, first_name, second_name) VALUES ('admin', '{bcrypt}$2a$08$Pv4nroY15eq9S0Fqs7LxMeibRr521vHzhhH85GIsREOK2zyJNTQeS', 'admin', 'admin');

INSERT INTO user_role(role_id, user_id) VALUES (1,1)