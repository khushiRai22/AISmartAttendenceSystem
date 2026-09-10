package com.sa.SmartAttendanceAI.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceHistoryDTO {

    private Long attendanceId;
    private String subject;
    private LocalDate attendanceDate;
    private boolean present;
    private LocalDateTime checkInTime;
    private Double aciScore;
    private String aciLevel;
    private boolean manualEntry;

    public AttendanceHistoryDTO() {}

    // Getters & Setters
    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public Double getAciScore() { return aciScore; }
    public void setAciScore(Double aciScore) { this.aciScore = aciScore; }

    public String getAciLevel() { return aciLevel; }
    public void setAciLevel(String aciLevel) { this.aciLevel = aciLevel; }

    public boolean isManualEntry() { return manualEntry; }
    public void setManualEntry(boolean manualEntry) { this.manualEntry = manualEntry; }
}
