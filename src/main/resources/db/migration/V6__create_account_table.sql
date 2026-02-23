CREATE TABLE IF NOT EXISTS account (
    id     BIGSERIAL             PRIMARY KEY,
    email  VARCHAR(255) NOT NULL UNIQUE,
    name   VARCHAR(16)  NOT NULL,
    tag    VARCHAR(5)   NOT NULL,
    role   VARCHAR(16)  NOT NULL,
    region VARCHAR(16)  NOT NULL
);