--- Sequence ---------------------------------------
CREATE SEQUENCE INTERVENE_USER_ACCOUNT_ID_NUMBER
    START WITH     1
    INCREMENT BY   1
    CACHE 1
NO CYCLE;

--- Stored procedure ----------------------------------------
CREATE ALIAS IF NOT EXISTS GET_NEXT_INTERVENE_USER_ACCOUNT_ID FOR "uk.ac.ebi.gdp.intervene.user.manager.service.h2.H2StoredProcedure.getNextInterveneUserAccountId";

--- Enum constants ----------------------------------------
CREATE TYPE "user_account_status" AS ENUM (
	'PENDING_APPROVAL',
	'ACTIVE',
	'INACTIVE',
	'SUSPENDED',
	'DELETED'
);

CREATE TYPE "auth_user_account_status" AS ENUM (
	'ENABLED',
	'DISABLED'
);

CREATE TYPE "auth_user_account_provider" AS ENUM (
	'ELIXIR'
);

--- Tables ----------------------------------------
CREATE TABLE user_account (
    user_id varchar(15) NOT NULL,
    given_name varchar(2048) NOT NULL,
    family_name varchar(2048) NOT NULL,
    email_id varchar(2048) NOT NULL,
    status varchar(255) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT USER(),
    created_on TIMESTAMP NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT USER(),
    updated_on TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT user_account_pk PRIMARY KEY (user_id),
    CONSTRAINT uad_email_id_uk UNIQUE (email_id)
);

CREATE TABLE user_account_details (
    user_id varchar(15) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT USER(),
    created_on TIMESTAMP NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT USER(),
    updated_on TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uad_pk PRIMARY KEY (user_id),
    CONSTRAINT uad_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);

CREATE TABLE auth_user_account (
    auth_user_id varchar(2048) NOT NULL,
    user_id varchar(512) NOT NULL,
    auth_provider VARCHAR(255) NOT NULL,
    status varchar(255) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT USER(),
    created_on TIMESTAMP NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT USER(),
    updated_on TIMESTAMP NULL DEFAULT now(),
    CONSTRAINT aua_pk PRIMARY KEY (auth_user_id),
    CONSTRAINT aua_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);

