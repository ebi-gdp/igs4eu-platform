--- Intervene user account sequence ---

CREATE SEQUENCE INTERVENE_USER_ACCOUNT_ID_NUMBER
    START WITH     1
    INCREMENT BY   1
    CACHE 1
NO CYCLE;

--- Intervene next user account stored procedure ---

CREATE OR REPLACE procedure GET_NEXT_INTERVENE_USER_ACCOUNT_ID(user_account_id INOUT VARCHAR)
LANGUAGE plpgsql AS
$$
BEGIN
SELECT nextval('intervene_user_account_id_number') INTO user_account_id;
user_account_id := CONCAT('INTU', LPAD(user_account_id, 11, '0'));
END
$$;

--- Pipeline Ids sequence ---

CREATE SEQUENCE INTERVENE_PIPELINE_ID_NUMBER
    START WITH     1
    INCREMENT BY   1
    CACHE 1
NO CYCLE;

--- Next Pipeline Id stored procedure ---

CREATE OR REPLACE procedure GET_NEXT_INTERVENE_PIPELINE_ID(pipeline_id INOUT VARCHAR)
LANGUAGE plpgsql AS
$$
BEGIN
SELECT nextval('intervene_pipeline_id_number') INTO pipeline_id;
pipeline_id := CONCAT('INTP', LPAD(pipeline_id, 11, '0'));
END
$$;

--- Dataset Id sequence ---

CREATE SEQUENCE INTERVENE_DATASET_ID_NUMBER
    START WITH     1
    INCREMENT BY   1
    CACHE 1
NO CYCLE;

--- Next Dataset Id stored procedure ---

CREATE OR REPLACE procedure GET_NEXT_DATASET_ID(dataset_id INOUT VARCHAR)
LANGUAGE plpgsql AS
$$
BEGIN
SELECT nextval('intervene_dataset_id_number') INTO dataset_id;
dataset_id := CONCAT('INTD', LPAD(dataset_id, 11, '0'));
END
$$;

--- Fileset Id sequence ---

CREATE SEQUENCE INTERVENE_FILESET_ID_NUMBER
    START WITH     1
    INCREMENT BY   1
    CACHE 1
NO CYCLE;

--- Next Fileset Id stored procedure ---

CREATE OR REPLACE procedure GET_NEXT_FILESET_ID(fileset_id INOUT VARCHAR)
LANGUAGE plpgsql AS
$$
BEGIN
SELECT nextval('intervene_fileset_id_number') INTO fileset_id;
fileset_id := CONCAT('INTF', LPAD(fileset_id, 11, '0'));
END
$$;
