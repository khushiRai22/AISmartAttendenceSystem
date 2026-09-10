package com.sa.SmartAttendanceAI.dto;

/**
 * Request body sent by student when marking attendance.
 * Contains GPS location + face confidence from OpenCV.
 */
public class StudentCheckInRequest {

    private Long timetableId;
    private Long studentId;
    private Double studentLatitude;
    private Double studentLongitude;
    private Double faceConfidence;  // 0–100, from OpenCV on frontend

    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long t) { this.timetableId = t; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long s) { this.studentId = s; }

    public Double getStudentLatitude() { return studentLatitude; }
    public void setStudentLatitude(Double l) { this.studentLatitude = l; }

    public Double getStudentLongitude() { return studentLongitude; }
    public void setStudentLongitude(Double l) { this.studentLongitude = l; }

    public Double getFaceConfidence() { return faceConfidence; }
    public void setFaceConfidence(Double f) { this.faceConfidence = f; }
}
