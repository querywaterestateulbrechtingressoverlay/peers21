CREATE TABLE intensive_data(id int PRIMARY KEY, start_date DATE, end_date DATE);
CREATE TABLE tribe_data(id INT PRIMARY KEY AUTO_INCREMENT, tribe_id INT UNIQUE, name VARCHAR(255));
CREATE TABLE peer_base_data(id INT PRIMARY KEY AUTO_INCREMENT, login VARCHAR(255) UNIQUE, wave VARCHAR(255), intensive INT, tribe_id INT, FOREIGN KEY (tribe_id) REFERENCES tribe_data(tribe_id), FOREIGN KEY (intensive) REFERENCES intensive_data(id));
CREATE TABLE peer_mutable_data(id INT PRIMARY KEY AUTO_INCREMENT, peer_id INT UNIQUE, state VARCHAR(255), tribe_points INT, exp_value INT, peer_review_points INT, code_review_points INT, coins INT, FOREIGN KEY (peer_id) REFERENCES peer_base_data(id));
--CREATE TABLE peer_project_data