CREATE SCHEMA IF NOT EXISTS department;

CREATE TABLE IF NOT EXISTS department.departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT
);
