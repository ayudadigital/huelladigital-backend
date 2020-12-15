ALTER TABLE credentials
    ADD COLUMN phone_number NUMERIC(9,0) NULL;
ALTER TABLE credentials
    ADD COLUMN birth_date DATE NULL;

ALTER TABLE locations
    ADD COLUMN zip_code CHAR(5) NULL;

ALTER TABLE volunteers
    DROP COLUMN age;

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