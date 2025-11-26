-- Sample users with PBKDF2 hashes
-- Generated using edu.univ.erp.auth.PasswordHasher
-- All passwords are for testing/demo purposes only

TRUNCATE users_auth RESTART IDENTITY;

INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES
-- admin1 / password: admin123
(1, 'admin1', 'ADMIN', 'PBKDF2$120000$CazttQGbhur42Flh039EdQ==$MPPrIKCMW2risYe4vNggO9CSvdjSmwD67YZw4CPH4cw=', 'ACTIVE'),

-- inst1 / password: inst123
(3, 'inst1', 'INSTRUCTOR', 'PBKDF2$120000$BHtkyMDp9RbIJtpgwhzJkQ==$CV93YdUgZttBe0scgAjuLDgfRh0HWiN8kkr8Jn8boJU=', 'ACTIVE'),

-- stu1 / password: student1
(4, 'stu1', 'STUDENT', 'PBKDF2$120000$Z5RPBbNS07GaUfNmvsn+UQ==$cjTyB4PbVdW9sPzaqxGAQ/QcUOEbzdHfdJdweWc97sM=', 'ACTIVE'),

-- stu2 / password: student2
(5, 'stu2', 'STUDENT', 'PBKDF2$120000$Lk/nIf53sd9peBECdPh91w==$rxmdvnzLyLB/wk1P9tUz5aFG/GyKjgfuw24iuxFptuY=', 'ACTIVE');

-- Test Credentials:
-- Admin:      admin1 / admin123
-- Instructor: inst1 / inst123
-- Student 1:  stu1 / student1
-- Student 2:  stu2 / student2

