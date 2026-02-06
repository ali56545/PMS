CREATE DATABASE pms_db;

-- Connect to the database
\c pms_db

-- Create students table
CREATE TABLE students (
                          id SERIAL PRIMARY KEY,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL
);

-- Create subjects table
CREATE TABLE subjects (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL UNIQUE,
                          description VARCHAR(255)
);

-- Create junction table for many-to-many relationship
CREATE TABLE student_subjects (
                                  student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
                                  subject_id INTEGER NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
                                  PRIMARY KEY (student_id, subject_id)
);

-- Insert sample students
INSERT INTO students (first_name, last_name) VALUES
                                                 ('Evgenev', 'Ali'),
                                                 ('John', 'Smith'),
                                                 ('Emma', 'Johnson'),
                                                 ('Michael', 'Brown'),
                                                 ('Sarah', 'Davis');

-- Insert sample subjects
INSERT INTO subjects (name, description) VALUES
                                             ('Math', 'Mathematics and Calculus'),
                                             ('Physics', 'Classical and Modern Physics'),
                                             ('Chemistry', 'Organic and Inorganic Chemistry'),
                                             ('Computer Science', 'Programming and Algorithms'),
                                             ('English', 'Literature and Writing');


INSERT INTO student_subjects (student_id, subject_id) VALUES (1, 1);


INSERT INTO student_subjects (student_id, subject_id) VALUES (2, 1), (2, 2);


INSERT INTO student_subjects (student_id, subject_id) VALUES (3, 4), (3, 5);


SELECT
    s.first_name || ' ' || s.last_name AS student_name,
    sub.name AS subject_name
FROM students s
         JOIN student_subjects ss ON s.id = ss.student_id
         JOIN subjects sub ON ss.subject_id = sub.id
ORDER BY s.last_name, s.first_name, sub.name;