package com.sa.SmartAttendanceAI.dto;

import java.time.LocalDate;

public class ManualAttendanceRequest {

    private Long batchId;
    private Long facultyId;
    private String subject;
    private LocalDate date;
	public Long getBatchId() {
		return batchId;
	}
	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}
	public Long getFacultyId() {
		return facultyId;
	}
	public void setFacultyId(Long facultyId) {
		this.facultyId = facultyId;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}

    // getters & setters
    
}