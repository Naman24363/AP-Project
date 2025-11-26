DROP TABLE IF EXISTS settings;
DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS instructors;
DROP TABLE IF EXISTS students;

CREATE TABLE students(
  user_id INTEGER PRIMARY KEY,
  roll_no VARCHAR(20) UNIQUE NOT NULL,
  program VARCHAR(50),
  year INTEGER
);

CREATE TABLE instructors(
  user_id INTEGER PRIMARY KEY,
  department VARCHAR(50)
);

CREATE TABLE courses(
  course_id SERIAL PRIMARY KEY,
  code VARCHAR(20) UNIQUE NOT NULL,
  title VARCHAR(100) NOT NULL,
  credits INTEGER NOT NULL
);

CREATE TABLE sections(
  section_id SERIAL PRIMARY KEY,
  course_id INTEGER NOT NULL REFERENCES courses(course_id),
  instructor_user_id INTEGER,
  day_time VARCHAR(50) NOT NULL,
  room VARCHAR(50),
  capacity INTEGER NOT NULL CHECK (capacity >= 0),
  semester VARCHAR(10),
  year INTEGER
);

CREATE TABLE enrollments(
  enrollment_id SERIAL PRIMARY KEY,
  student_user_id INTEGER NOT NULL,
  section_id INTEGER NOT NULL REFERENCES sections(section_id) ON DELETE CASCADE,
  status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
  UNIQUE(student_user_id, section_id)
);

CREATE TABLE grades(
  grade_id SERIAL PRIMARY KEY,
  enrollment_id INTEGER NOT NULL REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
  component VARCHAR(20) NOT NULL,
  score NUMERIC(5,2),
  final_grade NUMERIC(5,2)
);

CREATE TABLE settings(
  key VARCHAR(50) PRIMARY KEY,
  value VARCHAR(100) NOT NULL
);
