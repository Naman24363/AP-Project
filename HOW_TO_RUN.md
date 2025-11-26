# University ERP System - How to Run

## Prerequisites
- Java Development Kit (JDK) 8 or higher
- PostgreSQL 10 or higher
- A terminal/command prompt
- At least 500MB of disk space

## Database Setup

### 1. Create Databases
```sql
CREATE DATABASE univ_auth;
CREATE DATABASE univ_erp;
```

### 2. Create Auth Database Tables and Seed Data
Connect to `univ_auth` database and run:
```bash
psql -U postgres -d univ_auth -f sql/auth_schema.sql
psql -U postgres -d univ_auth -f sql/auth_seed.sql
```

### 3. Create ERP Database Tables and Seed Data
Connect to `univ_erp` database and run:
```bash
psql -U postgres -d univ_erp -f sql/erp_schema.sql
psql -U postgres -d univ_erp -f sql/erp_seed.sql
```

## Configuration

### 1. Update Database Credentials
Edit `config/app.properties`:
```properties
# PostgreSQL JDBC configs
auth.url=jdbc:postgresql://localhost:5432/univ_auth
auth.user=postgres
auth.password=YOUR_PASSWORD

erp.url=jdbc:postgresql://localhost:5432/univ_erp
erp.user=postgres
erp.password=YOUR_PASSWORD

# App settings
app.title=University ERP (Java + Swing)
```

### 2. Add PostgreSQL JDBC Driver
The PostgreSQL driver is required. Add `postgresql-*.jar` to the classpath, or install via Maven/Gradle.

## Compilation

From the project root directory:
```bash
javac -d bin src/edu/univ/erp/Main.java \
  src/edu/univ/erp/util/*.java \
  src/edu/univ/erp/auth/*.java \
  src/edu/univ/erp/data/*.java \
  src/edu/univ/erp/access/*.java \
  src/edu/univ/erp/domain/*.java \
  src/edu/univ/erp/service/*.java \
  src/edu/univ/erp/ui/auth/*.java \
  src/edu/univ/erp/ui/admin/*.java \
  src/edu/univ/erp/ui/instructor/*.java \
  src/edu/univ/erp/ui/student/*.java \
  src/edu/univ/erp/ui/common/*.java
```

## Running the Application

```bash
cd bin
java -cp .:postgresql-<version>.jar edu.univ.erp.Main
```

Or on Windows:
```bash
cd bin
java -cp .;postgresql-<version>.jar edu.univ.erp.Main
```

## Default Test Accounts

| Username | Password | Role |
|----------|----------|------|
| admin1 | admin123 | Admin |
| inst1 | inst123 | Instructor |
| stu1 | student1 | Student |
| stu2 | student2 | Student |

## Features Overview

### Student
- Browse course catalog
- Register for sections (with automatic duplicate prevention)
- Drop sections before deadline
- View timetable
- View grades and final scores
- Download transcript as CSV

### Instructor
- View assigned sections
- Enter scores for assessments (Quiz, Midterm, End-Semester)
- Automatic final grade computation (20% Quiz, 30% Midterm, 50% End-Sem)
- View class statistics (averages, min, max)
- Export grades as CSV

### Admin
- Create students, instructors, and admin users
- Create courses and manage sections
- Assign instructors to sections
- Toggle maintenance mode
- View complete catalog

## Troubleshooting

### Cannot connect to database
- Verify PostgreSQL is running
- Check credentials in `config/app.properties`
- Ensure both databases exist and are initialized with schemas

### "Driver not found" error
- Add the PostgreSQL JDBC driver JAR to the classpath
- Check Java classpath configuration

### UI looks different
- Application uses FlatLaf for modern look (if available), falls back to Nimbus
- Ensure Segoe UI font is available on your system

## Database Schema Notes

### Two Separate Databases

**Auth DB (`univ_auth`):**
- Stores user authentication information
- Contains password hashes (PBKDF2-SHA256)
- No personal data stored here

**ERP DB (`univ_erp`):**
- Stores all academic and profile data
- Linked to Auth DB via user_id
- Contains courses, sections, enrollments, grades, and settings

### Important Settings

The `settings` table in ERP DB controls:
- `maintenance_on`: Set to 'true' or 'false' to toggle maintenance mode
- `drop_deadline`: The last date students can drop courses (format: YYYY-MM-DD)
- `grading_scale`: Reference grading scale (informational)

## Getting Help

- Check seed data files for expected database structure
- Review error messages carefully - they indicate validation issues
- Ensure maintenance mode is OFF before testing student/instructor features

---
**Last Updated:** November 2025
