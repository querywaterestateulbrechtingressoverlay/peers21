CREATE TABLE api_campuses (
    id INT PRIMARY KEY,
    short_name VARCHAR(255));
CREATE TABLE api_peer_data(
    id INT PRIMARY KEY,
    login VARCHAR(255),
    class_name VARCHAR(255),
    parallel_name VARCHAR(255),
    exp_value INT,
    exp_to_next_level INT,
    campus_id INT,
    state VARCHAR(255),
    FOREIGN KEY(id) REFERENCES api_peer_points_data(id),
    FOREIGN KEY(campus_id) REFERENCES api_campuses(id)
    );
CREATE TABLE api_peer_points_data(
    login VARCHAR(255) PRIMARY KEY,
    peer_review_points INT,
    code_review_points INT,
    coins INT);

CREATE TABLE intensives(id int PRIMARY KEY, start_date date, end_date date);

INSERT INTO INTENSIVES VALUES (1, '23-10-2023', '17-11-2023');
INSERT INTO INTENSIVES VALUES (2, '21-10-2023', '15-22-2023');
INSERT INTO INTENSIVES VALUES (3, '05-02-2024', '01-03-2024');
INSERT INTO INTENSIVES VALUES (4, '18-03-2024', '12-04-2024');
INSERT INTO INTENSIVES VALUES (5, '24-06-2024', '19-07-2024');
INSERT INTO INTENSIVES VALUES (6, '19-08-2024', '13-09-2024');
INSERT INTO INTENSIVES VALUES (7, '30-09-2024', '25-10-2024');
