-- Comprehensive seed data for ERP database
TRUNCATE grades RESTART IDENTITY CASCADE;
TRUNCATE enrollments RESTART IDENTITY CASCADE;
TRUNCATE sections RESTART IDENTITY CASCADE;
TRUNCATE courses RESTART IDENTITY CASCADE;
TRUNCATE instructors RESTART IDENTITY CASCADE;
TRUNCATE students RESTART IDENTITY CASCADE;
TRUNCATE settings;

-- Create student profiles
INSERT INTO students(user_id, roll_no, program, year) VALUES
(4, 'CSE-2024-001', 'B.Tech Computer Science', 2),
(5, 'ECE-2024-002', 'B.Tech Electronics & Communication', 2);

-- Create instructor profile  
INSERT INTO instructors(user_id, department) VALUES
(3, 'Computer Science & Engineering');

-- Create courses
INSERT INTO courses(code, title, credits) VALUES
('CS101', 'Introduction to Computer Science', 4),
('CS102', 'Data Structures', 4),
('CS201', 'Algorithms & Design', 4),
('MA101', 'Discrete Mathematics', 3),
('MA102', 'Linear Algebra', 3),
('EC101', 'Digital Electronics', 4);

-- Create sections (course offerings)
INSERT INTO sections(course_id, instructor_user_id, day_time, room, capacity, semester, year) VALUES
((SELECT course_id FROM courses WHERE code='CS101'), 3, 'Monday 10:00-11:30', 'A-101', 30, 'Odd', 2025),
((SELECT course_id FROM courses WHERE code='CS101'), 3, 'Wednesday 14:00-15:30', 'A-102', 30, 'Odd', 2025),
((SELECT course_id FROM courses WHERE code='CS102'), 3, 'Tuesday 09:00-10:30', 'B-201', 25, 'Odd', 2025),
((SELECT course_id FROM courses WHERE code='CS201'), 3, 'Thursday 11:00-12:30', 'B-301', 20, 'Even', 2025),
((SELECT course_id FROM courses WHERE code='MA101'), 3, 'Friday 10:00-11:30', 'C-101', 50, 'Odd', 2025),
((SELECT course_id FROM courses WHERE code='MA102'), 3, 'Monday 14:00-15:30', 'C-102', 40, 'Even', 2025),
((SELECT course_id FROM courses WHERE code='EC101'), 3, 'Wednesday 10:00-11:30', 'D-101', 25, 'Odd', 2025);

-- Sample enrollments for student 1 (stu1 - CSE student)
INSERT INTO enrollments(student_user_id, section_id, status) VALUES
(4, 1, 'ENROLLED'),
(4, 3, 'ENROLLED'),
(4, 5, 'ENROLLED');

-- Sample enrollments for student 2 (stu2 - ECE student)
INSERT INTO enrollments(student_user_id, section_id, status) VALUES
(5, 1, 'ENROLLED'),
(5, 5, 'ENROLLED'),
(5, 7, 'ENROLLED');

-- Sample grades for student 1 in CS101
INSERT INTO grades(enrollment_id, component, score) VALUES
(1, 'QUIZ', 85),
(1, 'MIDTERM', 78),
(1, 'ENDSEM', 82);

INSERT INTO grades(enrollment_id, component, final_grade) VALUES
(1, 'FINAL', 81.40);

-- Sample grades for student 1 in CS102
INSERT INTO grades(enrollment_id, component, score) VALUES
(2, 'QUIZ', 90),
(2, 'MIDTERM', 88),
(2, 'ENDSEM', 85);

INSERT INTO grades(enrollment_id, component, final_grade) VALUES
(2, 'FINAL', 87.00);

-- Sample grades for student 1 in MA101
INSERT INTO grades(enrollment_id, component, score) VALUES
(3, 'QUIZ', 75),
(3, 'MIDTERM', 72),
(3, 'ENDSEM', 80);

INSERT INTO grades(enrollment_id, component, final_grade) VALUES
(3, 'FINAL', 76.60);

-- Sample grades for student 2 in CS101
INSERT INTO grades(enrollment_id, component, score) VALUES
(4, 'QUIZ', 88),
(4, 'MIDTERM', 84),
(4, 'ENDSEM', 90);

INSERT INTO grades(enrollment_id, component, final_grade) VALUES
(4, 'FINAL', 87.60);

-- Sample grades for student 2 in MA101
INSERT INTO grades(enrollment_id, component, score) VALUES
(5, 'QUIZ', 92),
(5, 'MIDTERM', 89),
(5, 'ENDSEM', 94);

INSERT INTO grades(enrollment_id, component, final_grade) VALUES
(5, 'FINAL', 91.40);

-- Sample grades for student 2 in EC101
INSERT INTO grades(enrollment_id, component, score) VALUES
(6, 'QUIZ', 80),
(6, 'MIDTERM', 85),
(6, 'ENDSEM', 88);

INSERT INTO grades(enrollment_id, component, final_grade) VALUES
(6, 'FINAL', 85.40);

-- Settings
INSERT INTO settings(key, value) VALUES
('maintenance_on', 'false'),
('drop_deadline', '2025-12-31'),
('grading_scale', 'A: 90-100, B: 80-89, C: 70-79, D: 60-69, F: <60');

