INSERT INTO departments (name, description)
VALUES
  ('Engineering', 'Builds and maintains products'),
  ('HR', 'People operations and recruiting')
ON DUPLICATE KEY UPDATE name = VALUES(name);