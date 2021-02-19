ALTER TABLE contact_persons
    ADD COLUMN name VARCHAR(255) NOT NULL;
ALTER TABLE contact_persons
    ADD COLUMN surname VARCHAR(255) NOT NULL;
ALTER TABLE contact_persons
    ADD COLUMN phone_number VARCHAR(255) NOT NULL;

CREATE TABLE contact_person_profile(
   id                      VARCHAR(255) UNIQUE NOT NULL,
   surrogate_key           SERIAL PRIMARY KEY,
   name                    VARCHAR(255),
   surname                 VARCHAR(255),
   phone_number            VARCHAR(255)
);

ALTER TABLE contact_persons
    ADD COLUMN id_contact_person_profile VARCHAR(255);

ALTER TABLE contact_persons
    ADD CONSTRAINT FK_id_contact_person_profile
        FOREIGN KEY (id_contact_person_profile)
            REFERENCES contact_person_profile(id);