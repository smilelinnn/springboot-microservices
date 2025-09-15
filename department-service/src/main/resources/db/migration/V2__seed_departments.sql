INSERT INTO department.departments (name, description)
VALUES
  ('Engineering', 'Builds and maintains products'),
  ('HR', 'People operations and recruiting')
ON CONFLICT DO NOTHING;
