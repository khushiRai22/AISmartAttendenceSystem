package com.sa.SmartAttendanceAI.dto;

public class StartSessionRequest {

    private Long timetableId;
    private Long facultyId;

    private Double latitude;
    private Double longitude;
    private Double radiusMeters;
	public Long getTimetableId() {
		return timetableId;
	}
	public void setTimetableId(Long timetableId) {
		this.timetableId = timetableId;
	}
	public Long getFacultyId() {
		return facultyId;
	}
	public void setFacultyId(Long facultyId) {
		this.facultyId = facultyId;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getRadiusMeters() {
		return radiusMeters;
	}
	public void setRadiusMeters(Double radiusMeters) {
		this.radiusMeters = radiusMeters;
	}

    // getters & setters
}