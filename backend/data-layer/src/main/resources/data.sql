INSERT INTO api_users (login, password, role) VALUES  ('admin', 'adminpassword', 'ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO api_users (login, password, role) VALUES ('user', 'userpassword', 'USER') ON CONFLICT DO NOTHING;
