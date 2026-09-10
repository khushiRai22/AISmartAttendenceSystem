package com.sa.SmartAttendanceAI.dto;

import java.time.LocalDateTime;

public class AttendanceDTO {

    private Long attendanceId;
    private String rollNo;
    private String studentName;
    private LocalDateTime checkInTime;
    private Double aciScore;   // shown on live dashboard
    private String aciLevel;

    public AttendanceDTO(Long attendanceId, String rollNo,
                         String studentName, LocalDateTime checkInTime,
                         Double aciScore, String aciLevel) {
        this.attendanceId = attendanceId;
        this.rollNo = rollNo;
        this.studentName = studentName;
        this.checkInTime = checkInTime;
        this.aciScore = aciScore;
        this.aciLevel = aciLevel;
    }

    public Long getAttendanceId() { return attendanceId; }
    public String getRollNo() { return rollNo; }
    public String getStudentName() { return studentName; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public Double getAciScore() { return aciScore; }
    public String getAciLevel() { return aciLevel; }
}
