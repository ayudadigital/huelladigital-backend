CREATE TABLE email_confirmation
(
    id              SERIAL PRIMARY KEY NOT NULL,
    email           VARCHAR NOT NULL ,
    hash            VARCHAR NOT NULL UNIQUE ,
    created_on      TIMESTAMP
);

CREATE TABLE credentials
(
    id              SERIAL          PRIMARY KEY NOT NULL,
    email           VARCHAR         UNIQUE,
    hashed_password VARCHAR         NOT NULL,
    email_confirmed BOOLEAN         NOT NULL,
    email_confirmation_id INTEGER   NOT NULL,
    FOREIGN KEY (email_confirmation_id) REFERENCES email_confirmation(id)
);

CREATE TABLE roles
(
    id              SERIAL          PRIMARY KEY NOT NULL,
    name            VARCHAR(50)     UNIQUE NOT NULL
);

CREATE TABLE credential_roles
(
    credential_id     INTEGER NOT NULL,
    role_id     INTEGER NOT NULL,
    PRIMARY KEY (credential_id, role_id),
    FOREIGN KEY (credential_id) REFERENCES credentials(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE volunteer
(
    id              SERIAL PRIMARY KEY NOT NULL,
    credential_id   INTEGER NOT NULL,
    FOREIGN KEY (credential_id) REFERENCES credentials(id)
);

INSERT INTO roles VALUES (1, 'ADMIN');
INSERT INTO roles VALUES (2, 'VOLUNTEER');


