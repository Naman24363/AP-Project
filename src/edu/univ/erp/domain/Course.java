package edu.univ.erp.domain;

public class Course {
    public int courseId;
    public String code;
    public String title;
    public int credits;

    public Course(int courseId, String code, String title, int credits) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    @Override
    public String toString() {
        return "Course{" + "courseId=" + courseId + ", code='" + code + '\'' +
                ", title='" + title + '\'' + ", credits=" + credits + '}';
    }
}
