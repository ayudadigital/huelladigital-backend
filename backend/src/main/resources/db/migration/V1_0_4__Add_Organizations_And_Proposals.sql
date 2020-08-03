CREATE TABLE organizations
(
    id              SERIAL PRIMARY KEY NOT NULL,
    name            VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE organization_members
(
    id              SERIAL PRIMARY KEY NOT NULL,
    credential_id   INTEGER NOT NULL,
    organization_id INTEGER,
    FOREIGN KEY (credential_id) REFERENCES credentials(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE locations
(
    id              SERIAL PRIMARY KEY NOT NULL,
    province        VARCHAR(255) NOT NULL,
    town            VARCHAR(255) NOT NULL,
    address         VARCHAR(255) NOT NULL
);

CREATE TABLE proposals
(
    id              SERIAL PRIMARY KEY NOT NULL,
    title           VARCHAR(255) NOT NULL,
    organization_id INTEGER NOT NULL,
    location_id     INTEGER NOT NULL,
    required_days   VARCHAR(255) NOT NULL,
    min_age         INTEGER NOT NULL,
    max_age         INTEGER NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    published       BOOLEAN NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE volunteers_proposals
(
    proposal_id INTEGER NOT NULL,
    volunteer_id INTEGER NOT NULL,
    PRIMARY KEY (proposal_id,volunteer_id),
    FOREIGN KEY (proposal_id) REFERENCES proposals(id),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
);

INSERT INTO roles VALUES (4, 'ORGANIZATION_MEMBER');
INSERT INTO roles VALUES (5, 'ORGANIZATION_MEMBER_NOT_CONFIRMED');
