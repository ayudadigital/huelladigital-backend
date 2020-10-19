ALTER TABLE credentials
ADD COLUMN hash_recovery_password VARCHAR UNIQUE;

ALTER TABLE credentials
ADD COLUMN created_recovery_hash_on TIMESTAMP;