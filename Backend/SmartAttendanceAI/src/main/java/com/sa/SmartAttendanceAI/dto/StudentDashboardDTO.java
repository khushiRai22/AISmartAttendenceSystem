package com.sa.SmartAttendanceAI.dto;

import java.util.List;

public class StudentDashboardDTO {

    private String studentName;
    private String rollNo;
    private String batchName;
    private long totalClasses;
    private long presentClasses;
    private double attendancePercentage;
    private List<ActiveSessionDTO> activeSessions;
    private List<TodayClassDTO> todayClasses;

    public StudentDashboardDTO() {}

    // Getters & Setters
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public long getTotalClasses() { return totalClasses; }
    public void setTotalClasses(long totalClasses) { this.totalClasses = totalClasses; }

    public long getPresentClasses() { return presentClasses; }
    public void setPresentClasses(long presentClasses) { this.presentClasses = presentClasses; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public List<ActiveSessionDTO> getActiveSessions() { return activeSessions; }
    public void setActiveSessions(List<ActiveSessionDTO> activeSessions) { this.activeSessions = activeSessions; }

    public List<TodayClassDTO> getTodayClasses() { return todayClasses; }
    public void setTodayClasses(List<TodayClassDTO> todayClasses) { this.todayClasses = todayClasses; }
}
