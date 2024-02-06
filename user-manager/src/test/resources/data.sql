INSERT INTO user_account (user_id, given_name, family_name, email_id, status, created_by,
                          created_on, updated_by, updated_on)
VALUES ('INTU00000000002', 'Intervene_User2_Given_Name', 'Intervene_User2_Family_Name', 'intervene_user2@ebi.ac.uk',
        'ACTIVE', 'INTU00000000002', '2024-01-26 13:09:36', 'INTU00000000002', '2024-01-26 13:09:36');

INSERT INTO user_account_details (user_id, created_by, created_on, updated_by, updated_on)
VALUES ('INTU00000000002', 'INTU00000000002', '2024-01-26 13:09:36', 'INTU00000000002', '2024-01-26 13:09:36');

INSERT INTO auth_user_account (auth_user_id, user_id, auth_provider, status, created_by, created_on,
                               updated_by, updated_on)
VALUES ('d75vb06ftg123cfd789f19d93e79c123c56982ff@elixir-europe.org', 'INTU00000000002', 'ELIXIR', 'ENABLED',
        'INTU00000000002', '2024-01-26 13:09:36', 'INTU00000000002', '2024-01-26 13:09:36');
