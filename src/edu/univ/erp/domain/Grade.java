package edu.univ.erp.domain;

import java.math.BigDecimal;

public class Grade {
    public int gradeId;
    public int enrollmentId;
    public String component;
    public BigDecimal score;
    public BigDecimal finalGrade;

    public Grade(int gradeId, int enrollmentId, String component, BigDecimal score, BigDecimal finalGrade) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
    }

    @Override
    public String toString() {
        return "Grade{" + "gradeId=" + gradeId + ", enrollmentId=" + enrollmentId +
                ", component='" + component + '\'' + ", score=" + score + ", finalGrade=" +
                finalGrade + '}';
    }
}
