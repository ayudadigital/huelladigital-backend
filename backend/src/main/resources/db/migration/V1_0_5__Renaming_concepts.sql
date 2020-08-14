--DROP TABLE email_confirmation CASCADE;
--
--
--DROP TABLE locations CASCADE;
--
--DROP TABLE credentials_roles CASCADE;
--
--DROP TABLE credentials CASCADE;
--DROP TABLE roles CASCADE;

--ALTER TABLE organizations
--RENAME TO ESALs;

--ALTER TABLE organization_members
--RENAME TO contact_persons;

--ALTER TABLE contact_persons
--RENAME COLUMN organization_id TO esal_id;
--
--ALTER TABLE proposals
--RENAME COLUMN organization_id TO esal_id;


DROP TABLE organization_members CASCADE;

DROP TABLE volunteers_proposals CASCADE;

DROP TABLE volunteer CASCADE;

DROP TABLE organizations CASCADE;

DROP TABLE proposals CASCADE;

DROP TABLE locations CASCADE;

CREATE TABLE ESALs (
    id              VARCHAR(255) UNIQUE NOT NULL,
    surrogate_key   SERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE contact_persons
(
    id              VARCHAR(255) UNIQUE NOT NULL,
    surrogate_key   SERIAL PRIMARY KEY,
    credential_id   INTEGER NOT NULL,
    esal_id         VARCHAR(255),
    FOREIGN KEY (credential_id) REFERENCES credentials(id),
    FOREIGN KEY (esal_id) REFERENCES ESALs(id)
);

CREATE TABLE locations
(
    id              VARCHAR(255) UNIQUE NOT NULL,
    surrogate_key   SERIAL PRIMARY KEY,
    province        VARCHAR(255) NOT NULL,
    town            VARCHAR(255) NOT NULL,
    address         VARCHAR(255) NOT NULL
);

CREATE TABLE proposals
(
    id              VARCHAR(255) UNIQUE NOT NULL,
    surrogate_key   SERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    esal_id         VARCHAR(255) NOT NULL,
    location_id     VARCHAR(255) NOT NULL,
    required_days   VARCHAR(255) NOT NULL,
    min_age         INTEGER NOT NULL,
    max_age         INTEGER NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    published       BOOLEAN NOT NULL,
    FOREIGN KEY (esal_id) REFERENCES ESALs(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE volunteers
(
    id              VARCHAR(255) UNIQUE NOT NULL,
    surrogate_key   SERIAL PRIMARY KEY,
    credential_id   INTEGER NOT NULL,
    age             INTEGER,
    location_id     VARCHAR(255),
    FOREIGN KEY (credential_id) REFERENCES credentials(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE volunteers_proposals
(
    proposal_id VARCHAR(255) NOT NULL,
    volunteer_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (proposal_id,volunteer_id),
    FOREIGN KEY (proposal_id) REFERENCES proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (volunteer_id) REFERENCES volunteers(id) ON DELETE CASCADE
);

UPDATE roles
SET NAME = 'CONTACT_PERSON' WHERE NAME = 'ORGANIZATION_MEMBER';

UPDATE roles
SET NAME = 'CONTACT_PERSON_NOT_CONFIRMED' WHERE NAME = 'ORGANIZATION_MEMBER_NOT_CONFIRMED';

UPDATE roles
SET NAME = 'REVISER' WHERE NAME = 'ADMIN';

