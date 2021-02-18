ALTER TABLE esals
    ADD COLUMN logo_url VARCHAR(255);
ALTER TABLE esals
    ADD COLUMN website VARCHAR(255);
ALTER TABLE esals
    ADD COLUMN description VARCHAR(500);
ALTER TABLE esals
    ADD COLUMN location_id VARCHAR(255);
ALTER TABLE esals
    ADD CONSTRAINT esals_location_id_fkey FOREIGN KEY (location_id) REFERENCES locations(id);
ALTER TABLE esals
    ADD COLUMN registered_entity BOOLEAN;
ALTER TABLE esals
    ADD COLUMN entity_type VARCHAR(255);
ALTER TABLE esals
    ADD COLUMN privacy_policy BOOLEAN DEFAULT TRUE;
ALTER TABLE esals
    ADD COLUMN data_protection_policy BOOLEAN DEFAULT TRUE;