ALTER TABLE reviser DROP CONSTRAINT reviser_credential_id_fkey;
ALTER TABLE volunteers DROP CONSTRAINT volunteers_credential_id_fkey;
ALTER TABLE contact_persons DROP CONSTRAINT contact_persons_credential_id_fkey;
DROP TABLE credential_roles;
ALTER TABLE credentials DROP CONSTRAINT credentials_pkey;

ALTER TABLE reviser DROP COLUMN credential_id;
ALTER TABLE reviser ADD COLUMN credential_id VARCHAR(255) NOT NULL;
ALTER TABLE volunteers DROP COLUMN credential_id;
ALTER TABLE volunteers ADD COLUMN credential_id VARCHAR(255) NOT NULL;
ALTER TABLE contact_persons DROP COLUMN credential_id;
ALTER TABLE contact_persons ADD COLUMN credential_id VARCHAR(255) NOT NULL;

ALTER TABLE credentials DROP COLUMN id;
ALTER TABLE credentials ADD COLUMN id VARCHAR(255) UNIQUE NOT NULL;
ALTER TABLE credentials ADD COLUMN surrogate_key SERIAL PRIMARY KEY;

ALTER TABLE reviser ADD CONSTRAINT reviser_credential_id_fkey FOREIGN KEY (credential_id) REFERENCES credentials(id);
ALTER TABLE volunteers ADD CONSTRAINT volunteers_credential_id_fkey FOREIGN KEY (credential_id) REFERENCES credentials(id);
ALTER TABLE contact_persons ADD CONSTRAINT contact_persons_credential_id_fkey FOREIGN KEY (credential_id) REFERENCES credentials(id);


CREATE TABLE credential_roles
(
    credential_id INTEGER NOT NULL,
    role_id     INTEGER NOT NULL,
    PRIMARY KEY (credential_id, role_id),
    FOREIGN KEY (credential_id) REFERENCES credentials(surrogate_key),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
