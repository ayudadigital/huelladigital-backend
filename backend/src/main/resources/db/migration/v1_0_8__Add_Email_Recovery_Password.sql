CREATE TABLE email_recovery_password
(
    id              SERIAL PRIMARY KEY NOT NULL,
    email           VARCHAR NOT NULL ,
    hash            VARCHAR NOT NULL UNIQUE ,
    sent_on         TIMESTAMP
);