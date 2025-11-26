package edu.univ.erp.domain;

public class Instructor {
    public int userId;
    public String department;

    public Instructor(int userId, String department) {
        this.userId = userId;
        this.department = department;
    }

    @Override
    public String toString() {
        return "Instructor{" + "userId=" + userId + ", department='" + department + '\'' + '}';
    }
}
