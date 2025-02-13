INSERT INTO api_users (login, password, role) VALUES ('admin', 'password', 'ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO api_users (login, password, role) VALUES ('user', 'password', 'USER') ON CONFLICT DO NOTHING;

INSERT INTO tribe_data VALUES (1, 213, 'qweqwe');
INSERT INTO peer_data VALUES (1, 'abc', 'zxc', 213, 'ACTIVE', 1, 2, 3, 4, 5) ON CONFLICT DO NOTHING;
