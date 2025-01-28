CREATE TABLE IF NOT EXISTS intensive_data(id int PRIMARY KEY, start_date DATE, end_date DATE);
CREATE TABLE IF NOT EXISTS tribe_data(id SERIAL PRIMARY KEY, tribe_id INT UNIQUE, name VARCHAR(255));
CREATE TABLE IF NOT EXISTS peer_base_data(id SERIAL PRIMARY KEY, login VARCHAR(255) UNIQUE, wave VARCHAR(255), intensive INT, tribe_id INT, FOREIGN KEY (tribe_id) REFERENCES tribe_data(tribe_id), FOREIGN KEY (intensive) REFERENCES intensive_data(id));
CREATE TABLE IF NOT EXISTS peer_mutable_data(id SERIAL PRIMARY KEY, peer_id INT UNIQUE, state VARCHAR(255), tribe_points INT, exp_value INT, peer_review_points INT, code_review_points INT, coins INT, FOREIGN KEY (peer_id) REFERENCES peer_base_data(id));
--CREATE TABLE peer_project_data
