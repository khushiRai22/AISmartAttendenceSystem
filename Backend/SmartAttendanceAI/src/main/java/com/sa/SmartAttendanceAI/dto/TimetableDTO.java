package com.sa.SmartAttendanceAI.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Timetable DTO for student timetable page
 * Includes facultyName as flat field (no nested object)
 */
public class TimetableDTO {

    private Long id;
    private String subject;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String facultyName;
    private String batchName;
    private String type; // Theory / Lab / Tutorial

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String v) { this.subject = v; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek v) { this.dayOfWeek = v; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime v) { this.startTime = v; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime v) { this.endTime = v; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String v) { this.facultyName = v; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String v) { this.batchName = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
}
