-- Add code column to departments table
ALTER TABLE departments
    ADD COLUMN code VARCHAR(20) NOT NULL DEFAULT '';

-- Update existing records with appropriate codes BEFORE adding unique constraint
UPDATE departments SET code = 'ENG' WHERE name = 'Engineering';
UPDATE departments SET code = 'HR' WHERE name = 'HR';

-- Add unique constraint for code column AFTER updating data
ALTER TABLE departments
    ADD CONSTRAINT uk_departments_code UNIQUE (code);

-- Remove the default value after updating existing records
ALTER TABLE departments
    ALTER COLUMN code DROP DEFAULT;