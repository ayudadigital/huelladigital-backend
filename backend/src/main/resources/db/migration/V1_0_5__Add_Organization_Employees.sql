DROP TABLE organization CASCADE;

CREATE TABLE organization
(
    id              SERIAL PRIMARY KEY NOT NULL,
    name            VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE organization_users(
    id              SERIAL PRIMARY KEY NOT NULL,
    credential_id   INTEGER NOT NULL,
    organization_id INTEGER,
    FOREIGN KEY (credential_id) REFERENCES credentials(id),
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);

UPDATE roles SET name = 'ORGANIZATION_EMPLOYEE' WHERE name = 'ORGANIZATION';
UPDATE roles SET name = 'ORGANIZATION_EMPLOYEE_NOT_CONFIRMED' WHERE name = 'ORGANIZATION_NOT_CONFIRMED';
