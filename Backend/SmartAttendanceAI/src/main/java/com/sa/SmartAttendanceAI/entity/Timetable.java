package com.sa.SmartAttendanceAI.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Batch Relation
    @JsonIgnoreProperties({"students"})
    @ManyToOne
    @JoinColumn(name = "batch_id", nullable = false)
    private Batches batch;

    // Faculty Relation
    @JsonIgnoreProperties({"profilePic"})
    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    private String subject;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;
    
    
    private boolean isActive = true;
    private boolean attendanceStarted = false;
    
    

    public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isAttendanceStarted() {
		return attendanceStarted;
	}

	public void setAttendanceStarted(boolean attendanceStarted) {
		this.attendanceStarted = attendanceStarted;
	}

	public void setId(Long id) {
		this.id = id;
	}

	// Sirf display purpose ke liye
    private String classroomName;

    // ===== Getters & Setters =====

    public Long getId() {
        return id;
    }

    public Batches getBatch() {
        return batch;
    }

    public void setBatch(Batches batch) {
        this.batch = batch;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }
}