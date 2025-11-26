package edu.univ.erp.domain;

public class Enrollment {
    public int enrollmentId;
    public int studentUserId;
    public int sectionId;
    public String status;

    public Enrollment(int enrollmentId, int studentUserId, int sectionId, String status) {
        this.enrollmentId = enrollmentId;
        this.studentUserId = studentUserId;
        this.sectionId = sectionId;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Enrollment{" + "enrollmentId=" + enrollmentId + ", studentUserId=" + studentUserId +
                ", sectionId=" + sectionId + ", status='" + status + '\'' + '}';
    }
}
