CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(120) NOT NULL,
    last_name  VARCHAR(120) NOT NULL,
    email      VARCHAR(200) NOT NULL UNIQUE,
    department_id BIGINT
);
