ALTER TABLE proposals
ADD COLUMN image_url VARCHAR(255);

ALTER TABLE proposals
ADD COLUMN starting_proposal_date TIMESTAMP;

ALTER TABLE proposals
RENAME COLUMN expiration_date TO closing_proposal_date;

ALTER TABLE proposals
RENAME COLUMN starting_date TO starting_volunteering_date;

ALTER TABLE volunteers
ADD COLUMN curriculum_vitae_url VARCHAR(255);
