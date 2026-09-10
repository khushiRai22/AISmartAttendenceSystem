package com.sa.SmartAttendanceAI.dto;

import java.time.LocalDate;

/**
 * Student profile DTO — returned by GET /api/student/me
 */
public class StudentProfileDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String gender;
    private LocalDate dob;
    private String rollNo;
    private String status;
    private String batchName;
    private Long batchId;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getMobile() { return mobile; }
    public void setMobile(String v) { this.mobile = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate v) { this.dob = v; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String v) { this.rollNo = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String v) { this.batchName = v; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long v) { this.batchId = v; }
}
