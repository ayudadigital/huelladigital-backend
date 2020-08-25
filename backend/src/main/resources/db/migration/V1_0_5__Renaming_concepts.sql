DROP TABLE IF EXISTS organization_members CASCADE;

DROP TABLE IF EXISTS  volunteers_proposals CASCADE;

DROP TABLE IF EXISTS volunteer CASCADE;

DROP TABLE IF EXISTS organizations CASCADE;

DROP TABLE IF EXISTS proposals CASCADE;

DROP TABLE IF EXISTS locations CASCADE;

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
    title           VARCHAR(75) NOT NULL,
    esal_id         VARCHAR(255) NOT NULL,
    location_id     VARCHAR(255) NOT NULL,
    required_days   VARCHAR(255) NOT NULL,
    min_age         INTEGER NOT NULL,
    max_age         INTEGER NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    published       BOOLEAN NOT NULL,
    description        VARCHAR(200) NOT NULL,
    duration_in_days   VARCHAR(50) NOT NULL,
    category           VARCHAR(20) NOT NULL,
    starting_date      TIMESTAMP NOT NULL,
    extra_info      VARCHAR(255),
    instructions    VARCHAR(255),
    FOREIGN KEY (esal_id) REFERENCES ESALs(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE proposal_skills
(
    surrogate_key SERIAL PRIMARY KEY,
    name          VARCHAR(75),
    description   VARCHAR(100),
    proposal_id   VARCHAR(255),
    FOREIGN KEY (proposal_id) REFERENCES proposals(id)
);

CREATE TABLE proposal_requirements
(
    surrogate_key SERIAL PRIMARY KEY,
    name          VARCHAR(75),
    proposal_id   VARCHAR(255),
    FOREIGN KEY (proposal_id) REFERENCES proposals(id)
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

