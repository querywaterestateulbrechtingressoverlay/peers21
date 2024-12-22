CREATE TABLE api_campus_data (id VARCHAR(255) PRIMARY KEY, short_name VARCHAR(255));
CREATE TABLE api_peer_points_data(login VARCHAR(255) PRIMARY KEY, peer_review_points INT, code_review_points INT, coins INT);
CREATE TABLE api_peer_data(id INT PRIMARY KEY, login VARCHAR(255), class_name VARCHAR(255), parallel_name VARCHAR(255), exp_value INT, exp_to_next_level INT, campus_id INT, state VARCHAR(255), FOREIGN KEY(login) REFERENCES api_peer_points_data(login), FOREIGN KEY(campus_id) REFERENCES api_campuses(id) );

CREATE TABLE intensives(id int PRIMARY KEY, start_date date, end_date date);

INSERT INTO intensives VALUES (1, '2023-10-23', '2023-11-17');
INSERT INTO intensives VALUES (2, '2023-10-21', '2023-12-15');
INSERT INTO intensives VALUES (3, '2024-02-05', '2024-03-01');
INSERT INTO intensives VALUES (4, '2024-03-18', '2024-04-12');
INSERT INTO intensives VALUES (5, '2024-06-24', '2024-07-19');
INSERT INTO intensives VALUES (6, '2024-08-19', '2024-09-13');
INSERT INTO intensives VALUES (7, '2024-09-30', '2024-10-25');
