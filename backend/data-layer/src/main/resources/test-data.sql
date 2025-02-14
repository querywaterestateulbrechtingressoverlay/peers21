CREATE TABLE IF NOT EXISTS tribe_data(
    id SERIAL PRIMARY KEY,
    tribe_id INT UNIQUE,
    name VARCHAR(255));
CREATE TABLE IF NOT EXISTS peer_data(
    id SERIAL PRIMARY KEY,
    login VARCHAR(255) UNIQUE,
    wave VARCHAR(255),
    tribe_id INT,
    state VARCHAR(255),
    tribe_points INT,
    exp_value INT,
    peer_review_points INT,
    code_review_points INT,
    coins INT,
    FOREIGN KEY (tribe_id) REFERENCES tribe_data(tribe_id)
    );
CREATE TABLE IF NOT EXISTS api_users(
    id SERIAL PRIMARY KEY,
    login VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(255)
);

INSERT INTO tribe_data (tribe_id, name) VALUES (123, 'amogi');
INSERT INTO tribe_data (tribe_id, name) VALUES (321, 'abobi');

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('amogus1', 1, 123, 'ACTIVE', 2018615, 1, 2, 3);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('amogus2', 2, 123, 'ACTIVE', 2018615, 4, 5, 6);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('amogus3', 3, 123, 'ACTIVE', 2018615, 7, 8, 9);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('abobus1', 1, 321, 'ACTIVE', 20181116, 10, 11, 12);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('abobus2', 2, 321, 'ACTIVE', 20181116, 13, 14, 15);

INSERT INTO peer_data(login, wave, tribe_id, state, tribe_points, exp_value, peer_review_points, code_review_points, coins)
VALUES ('abobus3', 3, 321, 'ACTIVE', 20181116, 16, 17, 18);