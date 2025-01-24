CREATE TABLE api_campus_data (id VARCHAR(255) PRIMARY KEY, short_name VARCHAR(255), full_name VARCHAR(255));
CREATE TABLE intensives(id int PRIMARY KEY, start_date date, end_date date);
CREATE TABLE peer_data(id INT PRIMARY KEY AUTO_INCREMENT, login VARCHAR(255) UNIQUE, intensive INT, exp_value INT, state VARCHAR(255), peer_review_points INT, code_review_points INT, coins INT, FOREIGN KEY (intensive) REFERENCES intensives(id));
CREATE TABLE tribe_data(id INT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE tribe_participants(tribe_id INT, peer_id INT, FOREIGN KEY (tribe_id) REFERENCES tribe_data(id), FOREIGN KEY (peer_id) REFERENCES peer_data(id));