package com.sa.SmartAttendanceAI.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Timetable timetable;

    private Double facultyLatitude;
    private Double facultyLongitude;

    private Double radiusMeters;

    private boolean active;

    private LocalDateTime startedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timetable getTimetable() {
		return timetable;
	}

	public void setTimetable(Timetable timetable) {
		this.timetable = timetable;
	}

	public Double getFacultyLatitude() {
		return facultyLatitude;
	}

	public void setFacultyLatitude(Double facultyLatitude) {
		this.facultyLatitude = facultyLatitude;
	}

	public Double getFacultyLongitude() {
		return facultyLongitude;
	}

	public void setFacultyLongitude(Double facultyLongitude) {
		this.facultyLongitude = facultyLongitude;
	}

	public Double getRadiusMeters() {
		return radiusMeters;
	}

	public void setRadiusMeters(Double radiusMeters) {
		this.radiusMeters = radiusMeters;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

    // getters & setters
}