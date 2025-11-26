package edu.univ.erp.domain;

public class Student {
    public int userId;
    public String rollNo;
    public String program;
    public int year;

    public Student(int userId, String rollNo, String program, int year) {
        this.userId = userId;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }

    @Override
    public String toString() {
        return "Student{" + "userId=" + userId + ", rollNo='" + rollNo + '\'' +
                ", program='" + program + '\'' + ", year=" + year + '}';
    }
}
