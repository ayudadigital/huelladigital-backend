ALTER TABLE organization DROP CONSTRAINT organization_credential_id_fkey;

ALTER TABLE organization DROP COLUMN credential_id;

CREATE TABLE organization_users(
    id              SERIAL PRIMARY KEY NOT NULL,
    credential_id   INTEGER NOT NULL,
    organization_id INTEGER NOT NULL,
    FOREIGN KEY (credential_id) REFERENCES credentials(id),
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);

UPDATE roles SET name = 'ORGANIZATION_EMPLOYEE' WHERE name = 'ORGANIZATION';
UPDATE roles SET name = 'ORGANIZATION_EMPLOYEE_NOT_CONFIRMED' WHERE name = 'ORGANIZATION_NOT_CONFIRMED';
