CREATE TABLE fail_email_confirmation
(
    id              SERIAL PRIMARY KEY NOT NULL,
    email_address   VARCHAR NOT NULL ,
    exception_trace VARCHAR NOT NULL ,
    created_on      TIMESTAMP
);


