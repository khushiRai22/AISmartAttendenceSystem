package com.sa.SmartAttendanceAI.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Timetable timetable;

    private LocalDate attendanceDate;
    private boolean present;
    private boolean manualEntry;
    private LocalDateTime checkInTime;

    // ── ACI Fields (Attendance Confidence Index) ──────────
    private Double aciScore;           // 0.0 to 100.0
    private String aciLevel;           // HIGH / MEDIUM / LOW / SUSPICIOUS
    private Double gpsDistanceMeters;  // student's actual GPS distance from faculty
    private Double faceConfidence;     // OpenCV face match percentage (0–100)

    // ── Getters & Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Timetable getTimetable() { return timetable; }
    public void setTimetable(Timetable timetable) { this.timetable = timetable; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate d) { this.attendanceDate = d; }

    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }

    public boolean isManualEntry() { return manualEntry; }
    public void setManualEntry(boolean manualEntry) { this.manualEntry = manualEntry; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime t) { this.checkInTime = t; }

    public Double getAciScore() { return aciScore; }
    public void setAciScore(Double aciScore) { this.aciScore = aciScore; }

    public String getAciLevel() { return aciLevel; }
    public void setAciLevel(String aciLevel) { this.aciLevel = aciLevel; }

    public Double getGpsDistanceMeters() { return gpsDistanceMeters; }
    public void setGpsDistanceMeters(Double d) { this.gpsDistanceMeters = d; }

    public Double getFaceConfidence() { return faceConfidence; }
    public void setFaceConfidence(Double f) { this.faceConfidence = f; }
}
