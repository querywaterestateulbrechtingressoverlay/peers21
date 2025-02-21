INSERT INTO api_users (login, password) VALUES  ('admin', 'adminpassword');
INSERT INTO api_users (login, password) VALUES ('user', 'userpassword');

INSERT INTO api_user_authorities (api_user_login, authority) VALUES ('user', 'USER');
INSERT INTO api_user_authorities (api_user_login, authority) VALUES ('admin', 'USER');
INSERT INTO api_user_authorities (api_user_login, authority) VALUES ('admin', 'ADMIN');

INSERT INTO tribe_data (tribe_id, name) VALUES (123, 'amogi');
INSERT INTO tribe_data (tribe_id, name) VALUES (321, 'abobi');

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('amogus1', 'wave_1', 123, 'ACTIVE', 2018615, 1, 2, 3, 4);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('amogus2', 'wave_2', 123, 'ACTIVE', 2018615, 4, 5, 6, 7);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('amogus3', 'wave_3', 123, 'ACTIVE', 2018615, 7, 8, 9, 10);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('abobus1', 'wave_1', 321, 'ACTIVE', 20181116, 10, 11, 12, 13);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('abobus2', 'wave_2', 321, 'ACTIVE', 20181116, 13, 14, 15, 16);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('abobus3', 'wave_3', 321, 'ACTIVE', 20181116, 16, 17, 18, 19);
