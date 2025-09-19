INSERT INTO employees (first_name, last_name, email, department_id)
VALUES
  ('Alice', 'Nguyen', 'alice@example.com', 1),
  ('Bob', 'Martinez', 'bob@example.com', 1),
  ('Carla', 'Singh', 'carla@example.com', 2)
ON DUPLICATE KEY UPDATE email = VALUES(email);
