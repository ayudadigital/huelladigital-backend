ALTER TABLE contact_persons
    ADD COLUMN name VARCHAR(255) NOT NULL;
ALTER TABLE contact_persons
    ADD COLUMN surname VARCHAR(255) NOT NULL;
ALTER TABLE contact_persons
    ADD COLUMN phone_number VARCHAR(255) NOT NULL;