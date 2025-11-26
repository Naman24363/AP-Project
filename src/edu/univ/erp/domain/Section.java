package edu.univ.erp.domain;

public class Section {
    public int sectionId;
    public int courseId;
    public Integer instructorUserId;
    public String dayTime;
    public String room;
    public int capacity;
    public String semester;
    public int year;

    public Section(int sectionId, int courseId, Integer instructorUserId, String dayTime,
            String room, int capacity, String semester, int year) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.instructorUserId = instructorUserId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    @Override
    public String toString() {
        return "Section{" + "sectionId=" + sectionId + ", courseId=" + courseId +
                ", instructorUserId=" + instructorUserId + ", dayTime='" + dayTime + '\'' +
                ", room='" + room + '\'' + ", capacity=" + capacity + ", semester='" + semester +
                '\'' + ", year=" + year + '}';
    }
}
