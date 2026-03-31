--liquibase formatted sql

--changeset dzarembo:001
CREATE TABLE credentials
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT                   NOT NULL UNIQUE,
    login         VARCHAR(50)              NOT NULL UNIQUE,
    password_hash VARCHAR(255)             NOT NULL,
    role          VARCHAR(10)              NOT NULL,
    active        BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--rollback DROP TABLE IF EXISTS credentials;
