CREATE TABLE app_user (
    username VARCHAR(40) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL
);
