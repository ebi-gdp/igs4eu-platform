INSERT INTO user_account (user_id, given_name, family_name, email_id, status, created_by,
                          created_on, updated_by, updated_on)
VALUES ('INTU00000000001', 'Intervene_User2_Given_Name', 'Intervene_User2_Family_Name', 'intervene_user2@ebi.ac.uk',
        'ACTIVE', 'INTU00000000001', '2024-01-26 13:09:36', 'INTU00000000001', '2024-01-26 13:09:36');

INSERT INTO user_account_details (user_id, created_by, created_on, updated_by, updated_on)
VALUES ('INTU00000000001', 'INTU00000000001', '2024-01-26 13:09:36', 'INTU00000000001', '2024-01-26 13:09:36');

INSERT INTO auth_user_account (auth_user_id, user_id, auth_provider, status, created_by, created_on,
                               updated_by, updated_on)
VALUES ('d75vb06ftg123cfd789f19d93e79c123c56982ff@elixir-europe.org', 'INTU00000000001', 'ELIXIR', 'ENABLED',
        'INTU00000000001', '2024-01-26 13:09:36', 'INTU00000000001', '2024-01-26 13:09:36');


INSERT INTO globus_user_details (globus_username, globus_user_uid, intervene_user_id, created_by, created_on, updated_by, updated_on)
VALUES ('intervene_user2@ebi.ac.uk', '31a67e9a-e4f0-4e21-983c-a596d0addbe9', 'INTU00000000001', 'INTU00000000001',	'2024-09-12 21:25:50.576239+01',
        'INTU00000000001',	'2024-09-12 21:25:50.576239+01');

INSERT INTO globus_guest_collection_files_details (fileset_id, globus_username, guest_collection_id,
                                                   dir_path_on_guest_collection, created_by, created_on, updated_by,
                                                   updated_on)
VALUES ('INTF00000000001', 'intervene_user2@ebi.ac.uk', 'c1e6310c-11d5-4e8a-9443-211884f04c6f',
        '/Sample-Set-Thursday-12-Sep-21-25-bdff2f10',
        'INTU00000000001', '2024-09-12 21:25:50.576239+01', 'INTU00000000001', '2024-09-12 21:25:50.576239+01');

INSERT INTO dataset_details (dataset_id, dataset_name, genome_build, fileset_id, fileset_type, created_by, created_on,
                             updated_by, updated_on, expires_at)
VALUES ('INTD00000000001', 'Sample-Set-Thursday-12-Sep-21-25', 'GRCH38', 'INTF00000000001', 'GLOBUS', 'INTU00000000001',
        '2024-09-12 21:25:50.576239+01', 'INTU00000000001', '2024-09-12 21:25:50.576239+01',
        '2024-10-12 21:25:49.34876+01');

