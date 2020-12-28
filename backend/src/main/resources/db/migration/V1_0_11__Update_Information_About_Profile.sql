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
    name                    VARCHAR(255) NOT NULL,
    surname                 VARCHAR(255) NOT NULL,
    phone_number            VARCHAR(255) NOT NULL,
    birth_date              DATE NOT NULL,
    location_id             VARCHAR(255),
    curriculum_vitae_url    VARCHAR(255),
    photo_url               VARCHAR(255),
    twitter                 VARCHAR(255),
    instagram               VARCHAR(255),
    linkedin                VARCHAR(255),
    additional_information  VARCHAR(500),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

ALTER TABLE volunteers
    ADD COLUMN twitter VARCHAR(255) NULL;
ALTER TABLE volunteers
    ADD COLUMN instagram VARCHAR(255) NULL;
ALTER TABLE volunteers
    ADD COLUMN linkedin VARCHAR(255) NULL;
ALTER TABLE volunteers
    ADD COLUMN photo VARCHAR(255);
ALTER TABLE volunteers
    ADD COLUMN additional_information VARCHAR(500);
ALTER TABLE volunteers
    ADD COLUMN profile_id VARCHAR(255);
ALTER TABLE volunteers
    ADD CONSTRAINT volunteers_profile_id_fkey
    FOREIGN KEY (profile_id)
    REFERENCES profile(id);
