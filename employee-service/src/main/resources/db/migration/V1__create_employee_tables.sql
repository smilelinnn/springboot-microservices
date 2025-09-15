CREATE SCHEMA IF NOT EXISTS employee;

CREATE TABLE IF NOT EXISTS employee.employees (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(120) NOT NULL,
    last_name  VARCHAR(120) NOT NULL,
    email      VARCHAR(200) NOT NULL UNIQUE,
    department_id BIGINT
);
