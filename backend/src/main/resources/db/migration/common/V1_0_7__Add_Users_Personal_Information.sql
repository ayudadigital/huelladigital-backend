ALTER TABLE credentials
ADD COLUMN name VARCHAR(255);

ALTER TABLE credentials
ADD COLUMN surname VARCHAR(255);

ALTER TABLE proposals
DROP COLUMN published;

ALTER TABLE proposals
ADD COLUMN status_id INTEGER DEFAULT(1);

CREATE TABLE statuses (
    id              INTEGER         UNIQUE NOT NULL,
    surrogate_key   SERIAL          PRIMARY KEY NOT NULL,
    name            VARCHAR(50)     UNIQUE NOT NULL
);

ALTER TABLE proposals
ADD CONSTRAINT FK_proposal_status FOREIGN KEY (status_id) REFERENCES statuses(id);

INSERT INTO statuses (id, name) values
(1, 'review_pending'),
(2, 'changes_requested'),
(3, 'published'),
(4, 'unpublished'),
(5, 'finished'),
(99, 'inadequate');
