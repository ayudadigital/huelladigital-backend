CREATE TABLE organization
(
    id              SERIAL PRIMARY KEY NOT NULL,
    credential_id   INTEGER NOT NULL,
    name            VARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (credential_id) REFERENCES credentials(id)
);

CREATE TABLE location
(
    id              SERIAL PRIMARY KEY NOT NULL,
    province        VARCHAR(255) NOT NULL,
    town            VARCHAR(255) NOT NULL,
    address         VARCHAR(255) NOT NULL
);

CREATE TABLE proposal
(
    id              SERIAL PRIMARY KEY NOT NULL,
    title           VARCHAR(255) NOT NULL,
    organization_id INTEGER NOT NULL,
    location_id     INTEGER NOT NULL,
    required_days   VARCHAR(255) NOT NULL,
    min_age         INTEGER NOT NULL,
    max_age         INTEGER NOT NULL,
    expiration_date DATE NOT NULL,
    published       BOOLEAN NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organization(id),
    FOREIGN KEY (location_id) REFERENCES location(id)
);

CREATE TABLE volunteer_proposal
(
    proposal_id INTEGER NOT NULL,
    volunteer_id INTEGER NOT NULL,
    PRIMARY KEY (proposal_id,volunteer_id),
    FOREIGN KEY (proposal_id) REFERENCES proposal(id),
    FOREIGN KEY (volunteer_id) REFERENCES volunteer(id)
);

INSERT INTO roles VALUES (4, 'ORGANIZATION');
INSERT INTO roles VALUES (5, 'ORGANIZATION_NOT_CONFIRMED');