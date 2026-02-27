ALTER TABLE IF EXISTS account
    ADD COLUMN permissions JSONB NOT NULL DEFAULT '{ "base_use": true }';