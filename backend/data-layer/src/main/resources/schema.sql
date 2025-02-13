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