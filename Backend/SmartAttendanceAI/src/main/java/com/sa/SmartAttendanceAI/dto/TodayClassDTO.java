package com.sa.SmartAttendanceAI.dto;

import java.time.LocalTime;

public class TodayClassDTO {

    private Long timetableId;
    private String subject;
    private String facultyName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // LIVE, UPCOMING, DONE, ABSENT
    private Double aciScore;
    private String aciLevel;

    public TodayClassDTO() {}

    // Getters & Setters
    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getAciScore() { return aciScore; }
    public void setAciScore(Double aciScore) { this.aciScore = aciScore; }

    public String getAciLevel() { return aciLevel; }
    public void setAciLevel(String aciLevel) { this.aciLevel = aciLevel; }
}
