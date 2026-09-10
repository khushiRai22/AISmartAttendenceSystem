package com.sa.SmartAttendanceAI.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ActiveSessionDTO {

    private Long timetableId;
    private Long sessionId;
    private String subject;
    private String facultyName;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime sessionStartedAt;
    private boolean alreadyMarked;

    public ActiveSessionDTO() {}

    // Getters & Setters
    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public LocalDateTime getSessionStartedAt() { return sessionStartedAt; }
    public void setSessionStartedAt(LocalDateTime sessionStartedAt) { this.sessionStartedAt = sessionStartedAt; }

    public boolean isAlreadyMarked() { return alreadyMarked; }
    public void setAlreadyMarked(boolean alreadyMarked) { this.alreadyMarked = alreadyMarked; }
}
