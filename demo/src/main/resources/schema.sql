CREATE TABLE api_campus_data (id VARCHAR(255) PRIMARY KEY, short_name VARCHAR(255), full_name VARCHAR(255));
CREATE TABLE intensives(id int PRIMARY KEY, start_date date, end_date date);
CREATE TABLE peer_data(id INT PRIMARY KEY AUTO_INCREMENT, login VARCHAR(255) UNIQUE, intensive INT, exp_value INT, state VARCHAR(255), peer_review_points INT, code_review_points INT, coins INT, FOREIGN KEY (intensive) REFERENCES intensives(id));
CREATE TABLE

INSERT INTO intensives VALUES (1, '2023-10-23', '2023-11-17');
INSERT INTO intensives VALUES (2, '2023-11-21', '2023-12-15');
INSERT INTO intensives VALUES (3, '2024-02-05', '2024-03-01');
INSERT INTO intensives VALUES (4, '2024-03-18', '2024-04-12');
INSERT INTO intensives VALUES (5, '2024-06-24', '2024-07-19');
INSERT INTO intensives VALUES (6, '2024-08-19', '2024-09-13');
INSERT INTO intensives VALUES (7, '2024-09-30', '2024-10-25');
INSERT INTO intensives VALUES (0, '1970-01-01', '1970-01-01');