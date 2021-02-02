ALTER TABLE credentials
    ADD COLUMN phone_number NUMERIC(9,0) NULL;
ALTER TABLE credentials
    ADD COLUMN birth_date DATE NULL;

ALTER TABLE locations
    ADD COLUMN zip_code CHAR(5) NOT NULL;
ALTER TABLE locations
    ADD COLUMN island VARCHAR(15) NOT NULL;
ALTER TABLE locations
    ALTER COLUMN province DROP NOT NULL;
ALTER TABLE locations
    ALTER COLUMN town DROP NOT NULL;
ALTER TABLE locations
    ALTER COLUMN address DROP NOT NULL;

ALTER TABLE volunteers
    DROP COLUMN age;

CREATE TABLE profile
(
    id                      VARCHAR(255) UNIQUE NOT NULL,
    surrogate_key           SERIAL PRIMARY KEY,
    name                    VARCHAR(255),
    surname                 VARCHAR(255),
    phone_number            VARCHAR(255),
    birth_date              DATE,
    curriculum_vitae_url    VARCHAR(1030),
    photo_url               VARCHAR(1030),
    twitter                 VARCHAR(255),
    instagram               VARCHAR(255),
    linkedin                VARCHAR(255),
    additional_information  VARCHAR(500)
);

ALTER TABLE volunteers
    DROP COLUMN curriculum_vitae_url;
ALTER TABLE volunteers
    ADD COLUMN profile_id VARCHAR(255);
ALTER TABLE volunteers
    ADD CONSTRAINT volunteers_profile_id_fkey
    FOREIGN KEY (profile_id)
    REFERENCES profile(id);

CREATE TABLE reviser
(
    id                      VARCHAR(255) UNIQUE NOT NULL,
    surrogate_key           SERIAL PRIMARY KEY,
    credential_id           INTEGER,
    name                    VARCHAR(255),
    surname                 VARCHAR(255),
    FOREIGN KEY (credential_id) REFERENCES credentials(id)
);

ALTER TABLE credentials
    DROP COLUMN name;
ALTER TABLE credentials
    DROP COLUMN surname;
ALTER TABLE credentials
    DROP COLUMN phone_number;
ALTER TABLE credentials
    DROP COLUMN birth_date;