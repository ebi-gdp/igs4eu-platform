--- Sequence ---------------------------------------
CREATE SEQUENCE INTERVENE_PIPELINE_ID_NUMBER
    START WITH 1
    INCREMENT BY 1 CACHE 1
NO CYCLE;

CREATE SEQUENCE INTERVENE_DATASET_ID_NUMBER
    START WITH 1
    INCREMENT BY 1 CACHE 1
NO CYCLE;

--- Stored procedure ----------------------------------------
CREATE
ALIAS IF NOT EXISTS GET_NEXT_INTERVENE_PIPELINE_ID FOR "uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.h2.H2StoredProcedure.getNextIntervenePipelineId";

CREATE
ALIAS IF NOT EXISTS GET_NEXT_DATASET_ID FOR "uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.h2.H2StoredProcedure.getNextInterveneDatasetId";

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

--- Tables ----------------------------------------
CREATE TABLE user_account (
    user_id     varchar(15)   NOT NULL,
    given_name  varchar(2048) NOT NULL,
    family_name varchar(2048) NOT NULL,
    email_id    varchar(2048) NOT NULL,
    status      varchar(255)  NOT NULL,
    created_by  varchar(15)   NOT NULL DEFAULT USER(),
    created_on  timestamp     NOT NULL DEFAULT now(),
    updated_by  varchar(15)   NOT NULL DEFAULT USER(),
    updated_on  timestamp     NOT NULL DEFAULT now(),
    CONSTRAINT user_account_pk PRIMARY KEY (user_id),
    CONSTRAINT uad_email_id_uk UNIQUE (email_id)
);

CREATE TABLE auth_user_account (
    auth_user_id  varchar(2048) NOT NULL,
    user_id       varchar(512)  NOT NULL,
    auth_provider varchar(255)  NOT NULL,
    status        varchar(255)  NOT NULL,
    created_by    varchar(15)   NOT NULL DEFAULT USER(),
    created_on    timestamp     NOT NULL DEFAULT now(),
    updated_by    varchar(15)   NOT NULL DEFAULT USER(),
    updated_on    timestamp NULL DEFAULT now(),
    CONSTRAINT aua_pk PRIMARY KEY (auth_user_id),
    CONSTRAINT aua_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account (user_id)
);

CREATE TABLE user_account_details (
    user_id    varchar(15) NOT NULL,
    created_by varchar(15) NOT NULL DEFAULT USER(),
    created_on timestamp   NOT NULL DEFAULT now(),
    updated_by varchar(15) NOT NULL DEFAULT USER(),
    updated_on timestamp   NOT NULL DEFAULT now(),
    CONSTRAINT uad_pk PRIMARY KEY (user_id),
    CONSTRAINT uad_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account (user_id)
);

CREATE TABLE globus_user_details (
    globus_username   varchar(512) NOT NULL,
    globus_user_uid   varchar(50)  NOT NULL,
    intervene_user_id varchar(15)  NOT NULL,
    created_by        varchar(15)  NOT NULL DEFAULT USER(),
    created_on        timestamp    NOT NULL DEFAULT now(),
    updated_by        varchar(15)  NOT NULL DEFAULT USER(),
    updated_on        timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT gud_pk PRIMARY KEY (globus_username),
    CONSTRAINT gud_int_user_id_ua_fk FOREIGN KEY (intervene_user_id) REFERENCES user_account (user_id)
);

CREATE TABLE globus_guest_collection_files_details (
    fileset_id                   varchar(15)   NOT NULL,
    globus_username              varchar(512)  NOT NULL,
    guest_collection_id          varchar(1024) NOT NULL,
    dir_path_on_guest_collection varchar(4092) NOT NULL,
    status                       "dataset_status_type" NULL,
    created_by                   varchar(15)   NOT NULL DEFAULT USER(),
    created_on                   timestamp     NOT NULL DEFAULT now(),
    updated_by                   varchar(15)   NOT NULL DEFAULT USER(),
    updated_on                   timestamp     NOT NULL DEFAULT now(),
    CONSTRAINT ggcfd_pk PRIMARY KEY (fileset_id),
    CONSTRAINT ggcfd_uk UNIQUE (dir_path_on_guest_collection),
    CONSTRAINT ggcfd_username_gud_fk FOREIGN KEY (globus_username) REFERENCES globus_user_details (globus_username)
);

CREATE TABLE dataset_details (
    dataset_id   varchar(15)  NOT NULL,
    dataset_name varchar(512) NOT NULL,
    genome_build varchar(255) NOT NULL,
    fileset_id   varchar(15)  NOT NULL,
    fileset_type varchar(255) NOT NULL,
    expires_at   timestamp    NOT NULL DEFAULT DATEADD('DAY', 1, CURRENT_TIMESTAMP) NOT NULL,
    is_deleted BOOLEAN DEFAULT false,
    created_by   varchar(15)  NOT NULL    DEFAULT USER(),
    created_on   timestamp    NOT NULL    DEFAULT now(),
    updated_by   varchar(15)  NOT NULL    DEFAULT USER(),
    updated_on   timestamp    NOT NULL    DEFAULT now(),
    CONSTRAINT dd_pk PRIMARY KEY (dataset_id),
    CONSTRAINT dd_fileset_id_ggcfd_fk FOREIGN KEY (fileset_id) REFERENCES globus_guest_collection_files_details (fileset_id)
);

CREATE TABLE dataset_cryptography_details (
    dataset_id varchar(15) NOT NULL,
    public_key varchar(256) NOT NULL,
    secret_id varchar(50) NOT NULL,
    secret_id_version varchar(512) NOT NULL,
    created_by   varchar(15)  NOT NULL DEFAULT USER(),
    created_on   timestamp    NOT NULL DEFAULT now(),
    updated_by   varchar(15)  NOT NULL DEFAULT USER(),
    updated_on   timestamp    NOT NULL DEFAULT now(),
    is_deleted BOOLEAN DEFAULT false,
    CONSTRAINT dcd_pk PRIMARY KEY (dataset_id),
    CONSTRAINT dcd_dataset_id_dd_fk FOREIGN KEY (dataset_id) REFERENCES dataset_details(dataset_id)
);

CREATE TABLE pipeline_details (
    pipeline_id  varchar(15)  NOT NULL,
    pipeline_uid varchar(512) NOT NULL,
    user_id      varchar(512) NOT NULL,
    dataset_id   varchar(15)  NOT NULL,
    created_by   varchar(15)  NOT NULL DEFAULT USER(),
    created_on   timestamp    NOT NULL DEFAULT now(),
    updated_by   varchar(15)  NOT NULL DEFAULT USER(),
    updated_on   timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT pd_pk PRIMARY KEY (pipeline_id),
    CONSTRAINT pd_uk UNIQUE (pipeline_uid),
    CONSTRAINT pd_user_id_ua_fk FOREIGN KEY (user_id) REFERENCES user_account (user_id),
    CONSTRAINT pd_dataset_id_dd_fk FOREIGN KEY (dataset_id) REFERENCES dataset_details (dataset_id)
);

CREATE TABLE pipeline_execution_status
(
    pipeline_id  varchar(15)  NOT NULL,
    status       varchar(255) NOT NULL,
    trace_name   varchar(512)          DEFAULT NULL,
    trace_exit   integer               DEFAULT NULL,
    submitted_on timestamp             DEFAULT NULL,
    started_on   timestamp             DEFAULT NULL,
    ended_on     timestamp             DEFAULT NULL,
    created_by   varchar(15)  NOT NULL DEFAULT USER(),
    created_on   timestamp    NOT NULL DEFAULT now(),
    updated_by   varchar(15)  NOT NULL DEFAULT USER(),
    updated_on   timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT ps_pk PRIMARY KEY (pipeline_id),
    CONSTRAINT pes_pipeline_id_pd_fk FOREIGN KEY (pipeline_id) REFERENCES pipeline_details (pipeline_id)
);

CREATE TABLE pipeline_result
(
    pipeline_id        varchar(15)   NOT NULL,
    file_download_path varchar(4096) NOT NULL,
    created_by         varchar(15)   NOT NULL DEFAULT USER(),
    created_on         timestamp     NOT NULL DEFAULT now(),
    updated_by         varchar(15)   NOT NULL DEFAULT USER(),
    updated_on         timestamp     NOT NULL DEFAULT now(),
    CONSTRAINT pr_pk PRIMARY KEY (pipeline_id),
    CONSTRAINT pr_pipeline_id_pd_fk FOREIGN KEY (pipeline_id) REFERENCES pipeline_details (pipeline_id)
);


