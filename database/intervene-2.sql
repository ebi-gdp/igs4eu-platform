--- SPRING Session ---

CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BYTEA NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

--- INTERVENE ---

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

CREATE TYPE "pipeline_status" AS ENUM (
	'NEW', 'STARTED', 'COMPLETED', 'FAILED', 'PENDING', 'ERROR'
);

CREATE TYPE "genome_build" AS ENUM (
	'GRCH38', 'GRCH37'
);

CREATE TYPE "fileset_type" AS ENUM (
	'GLOBUS'
);

CREATE TYPE "user_dpa_consent_type" AS ENUM (
	'NOT_GIVEN','GIVEN', 'REVOKED'
);

CREATE TYPE "dataset_status_type" AS ENUM (
	'ACTIVE','DELETED'
);

CREATE TABLE user_account (
    user_id varchar(15) NOT NULL,
    given_name varchar(2048) NOT NULL,
    family_name varchar(2048) NOT NULL,
    email_id varchar(2048) NOT NULL,
    status "user_account_status" NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT "current_user"(),
    created_on timestamptz NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
    updated_on timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT user_account_pk PRIMARY KEY (user_id),
    CONSTRAINT uad_email_id_uk UNIQUE (email_id)
);

CREATE TABLE auth_user_account (
    auth_user_id varchar(2048) NOT NULL,
    user_id varchar(512) NOT NULL,
    auth_provider "auth_user_account_provider" NOT NULL,
    status "auth_user_account_status" NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT "current_user"(),
    created_on timestamptz NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
    updated_on timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT aua_pk PRIMARY KEY (auth_user_id),
    CONSTRAINT aua_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);

CREATE TABLE user_account_details (
    user_id varchar(15) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT "current_user"(),
    created_on timestamptz NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
    updated_on timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uad_pk PRIMARY KEY (user_id),
    CONSTRAINT uad_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);

CREATE TABLE oauth2_authorized_client (
    client_registration_id varchar(100) NOT NULL,
    principal_name varchar(200) NOT NULL,
    access_token_type varchar(100) NOT NULL,
    access_token_value bytea NOT NULL,
    access_token_issued_at timestamptz NOT NULL,
    access_token_expires_at timestamptz NOT NULL,
    access_token_scopes varchar(1000) DEFAULT NULL,
    refresh_token_value bytea DEFAULT NULL,
    refresh_token_issued_at timestamptz DEFAULT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    PRIMARY KEY (client_registration_id, principal_name)
);

CREATE TABLE globus_user_details (
    globus_username varchar(512) NOT NULL,
    globus_user_uid varchar(50) NOT NULL,
    intervene_user_id varchar(15) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT "current_user"(),
    created_on timestamptz NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
    updated_on timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT gud_pk PRIMARY KEY (globus_username),
    CONSTRAINT gud_int_user_id_ua_fk FOREIGN KEY (intervene_user_id) REFERENCES user_account(user_id)
);

CREATE TABLE globus_guest_collection_files_details (
    fileset_id varchar(15) NOT NULL,
    globus_username varchar(512) NOT NULL,
    guest_collection_id varchar(1024) NOT NULL,
    dir_path_on_guest_collection varchar(4092) NOT NULL,
    status "dataset_status_type" NULL,
    created_by varchar(15) DEFAULT '"current_user"()' NOT NULL,
    created_on timestamptz DEFAULT now() NOT NULL,
    updated_by varchar(15) DEFAULT '"current_user"()' NOT NULL,
    updated_on timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT ggcfd_pk PRIMARY KEY (fileset_id),
    CONSTRAINT ggcfd_uk UNIQUE (dir_path_on_guest_collection),
    CONSTRAINT ggcfd_username_gud_fk FOREIGN KEY (globus_username) REFERENCES globus_user_details(globus_username)
);

CREATE TABLE dataset_details (
    dataset_id varchar(15) NOT NULL,
    dataset_name varchar(512) NOT NULL,
    genome_build "genome_build" NOT NULL,
    fileset_id varchar(15) NOT NULL,
    fileset_type "fileset_type" NOT NULL,
    expires_at timestamptz NOT NULL DEFAULT (now() + interval '28 days'),
    is_deleted bool DEFAULT false NULL,
    created_by varchar(15) DEFAULT "current_user"() NOT NULL,
    created_on timestamptz DEFAULT now() NOT NULL,
    updated_by varchar(15) DEFAULT "current_user"() NOT NULL,
    updated_on timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT dd_pk PRIMARY KEY (dataset_id),
    CONSTRAINT dd_fileset_id_ggcfd_fk FOREIGN KEY (fileset_id) REFERENCES globus_guest_collection_files_details(fileset_id)
);

CREATE TABLE dataset_cryptography_details (
    dataset_id varchar(15) NOT NULL,
    public_key varchar(256) NOT NULL,
    secret_id varchar(50) NOT NULL,
    secret_id_version varchar(512) NOT NULL,
    created_by varchar(15) DEFAULT "current_user"() NOT NULL,
    created_on timestamptz DEFAULT now() NOT NULL,
    updated_by varchar(15) DEFAULT "current_user"() NOT NULL,
    updated_on timestamptz DEFAULT now() NOT NULL,
    is_deleted bool DEFAULT false NULL,
    CONSTRAINT dcd_pk PRIMARY KEY (dataset_id),
    CONSTRAINT dcd_dataset_id_dd_fk FOREIGN KEY (dataset_id) REFERENCES dataset_details(dataset_id)
);

CREATE TABLE pipeline_details (
    pipeline_id varchar(15) NOT NULL,
    pipeline_uid varchar(512) NOT NULL,
    user_id varchar(512) NOT NULL,
    dataset_id varchar(15) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT "current_user"(),
    created_on timestamptz NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
    updated_on timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pd_pk PRIMARY KEY (pipeline_id),
    CONSTRAINT pd_uk UNIQUE (pipeline_uid),
    CONSTRAINT pd_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account(user_id),
    CONSTRAINT pd_dataset_id_dd_fk FOREIGN KEY (dataset_id) REFERENCES dataset_details(dataset_id)
);

CREATE TABLE pipeline_execution_status (
    pipeline_id varchar(15) NOT NULL,
    status "pipeline_status" NOT NULL,
    trace_name varchar(512) DEFAULT NULL,
    trace_exit smallint DEFAULT NULL,
    submitted_on timestamptz DEFAULT NULL,
    started_on timestamptz DEFAULT NULL,
    ended_on timestamptz DEFAULT NULL,
    created_by varchar(15) NOT NULL DEFAULT "current_user"(),
    created_on timestamptz NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
    updated_on timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ps_pk PRIMARY KEY (pipeline_id),
    CONSTRAINT pes_pipeline_id_pd_fk FOREIGN KEY (pipeline_id) REFERENCES pipeline_details(pipeline_id)
);

CREATE TABLE pipeline_result (
    pipeline_id varchar(15) NOT NULL,
    file_download_path varchar(4096) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT "current_user"(),
    created_on timestamptz NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
    updated_on timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pr_pk PRIMARY KEY (pipeline_id),
    CONSTRAINT pr_pipeline_id_pd_fk FOREIGN KEY (pipeline_id) REFERENCES pipeline_details(pipeline_id)
);

CREATE TABLE user_dpa_consent_details (
    consent_id varchar(15) NOT NULL,
    consent_text TEXT NOT NULL,
    version VARCHAR(10) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT "current_user"(),
    created_on timestamptz NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
    updated_on timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ucd_pk PRIMARY KEY (consent_id)
);

CREATE TABLE user_dpa_consent_audit_logs (
     user_id varchar(15) NOT NULL,
     consent_id varchar(15) NOT NULL,
     consent_type "user_dpa_consent_type" NOT NULL,
     created_by varchar(15) NOT NULL DEFAULT "current_user"(),
     created_on timestamptz NOT NULL DEFAULT now(),
     updated_by varchar(15) NOT NULL DEFAULT "current_user"(),
     updated_on timestamptz NOT NULL DEFAULT now(),
     CONSTRAINT ucal_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account(user_id),
     CONSTRAINT ucal_consent_id_ucd_fk FOREIGN KEY (consent_id) REFERENCES user_dpa_consent_details(consent_id)
);
