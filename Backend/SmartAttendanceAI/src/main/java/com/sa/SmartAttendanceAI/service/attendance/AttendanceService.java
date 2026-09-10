package com.sa.SmartAttendanceAI.service.attendance;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.SmartAttendanceAI.dto.AttendanceDTO;
import com.sa.SmartAttendanceAI.dto.LiveAttendanceResponse;
import com.sa.SmartAttendanceAI.dto.StartSessionRequest;
import com.sa.SmartAttendanceAI.dto.StudentCheckInRequest;
import com.sa.SmartAttendanceAI.dto.StudentCheckInResponse;
import com.sa.SmartAttendanceAI.entity.*;
import com.sa.SmartAttendanceAI.repository.*;

@Service
public class AttendanceService {

    @Autowired private TimetableRepository timetableRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private AttendanceSessionRepository sessionRepo;
    @Autowired private ACICalculatorService aciCalculator;

    // Haversine formula: GPS coordinates → distance in metres
    private double haversineDistance(double lat1, double lon1,
                                     double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1))
                 * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ── START SESSION ──────────────────────────────────────────────
    @Transactional
    public void startSession(StartSessionRequest req) {
        Timetable timetable = timetableRepo.findById(req.getTimetableId())
                .orElseThrow(() -> new RuntimeException("Timetable not found"));

        if (!timetable.getFaculty().getId().equals(req.getFacultyId()))
            throw new RuntimeException("Unauthorized faculty");

        sessionRepo.findByTimetableIdAndActiveTrue(req.getTimetableId())
                .ifPresent(old -> {
                    old.setActive(false);
                    if (old.getTimetable() != null) {
                        old.getTimetable().setAttendanceStarted(false);
                        timetableRepo.save(old.getTimetable());
                    }
                    sessionRepo.save(old);
                });

        sessionRepo.findAllByActiveTrue().stream()
                .filter(s -> s.getTimetable().getFaculty().getId().equals(req.getFacultyId()))
                .forEach(s -> {
                    s.setActive(false);
                    if (s.getTimetable() != null) {
                        s.getTimetable().setAttendanceStarted(false);
                        timetableRepo.save(s.getTimetable());
                    }
                    sessionRepo.save(s);
                });

        AttendanceSession session = new AttendanceSession();
        session.setTimetable(timetable);
        session.setFacultyLatitude(req.getLatitude());
        session.setFacultyLongitude(req.getLongitude());
        session.setRadiusMeters(req.getRadiusMeters());
        session.setActive(true);
        session.setStartedAt(LocalDateTime.now());
        sessionRepo.save(session);

        timetable.setAttendanceStarted(true);
        timetableRepo.save(timetable);
    }

    // ── END SESSION ────────────────────────────────────────────────
    @Transactional
    public void endSession(Long timetableId) {
        AttendanceSession session =
                sessionRepo.findByTimetableIdAndActiveTrue(timetableId)
                        .orElseThrow(() -> new RuntimeException("No active session"));
        session.setActive(false);
        sessionRepo.save(session);

        Timetable timetable = session.getTimetable();
        if (timetable != null) {
            timetable.setAttendanceStarted(false);
            timetableRepo.save(timetable);
        }
    }

    // ── STUDENT CHECK-IN ───────────────────────────────────────────
    public StudentCheckInResponse studentCheckIn(StudentCheckInRequest req) {

        // 1. Find active session
        AttendanceSession session =
                sessionRepo.findByTimetableIdAndActiveTrue(req.getTimetableId())
                        .orElseThrow(() -> new RuntimeException("No active session for this class"));

        // FIX 4: 50% window check
        long minutesSinceStart = Duration.between(
                session.getStartedAt(), LocalDateTime.now()).toMinutes();

        long sessionDurationSec = Duration.between(
                session.getTimetable().getStartTime(),
                session.getTimetable().getEndTime()).getSeconds();
        if (sessionDurationSec <= 0) sessionDurationSec = 2700;

        long halfDurationMin = (sessionDurationSec / 60) / 2;
        if (minutesSinceStart > halfDurationMin) {
            return StudentCheckInResponse.fail(
                "Check-in window closed. " + minutesSinceStart
                + " min passed. Limit is " + halfDurationMin + " min (50% of class).",
                "WINDOW_CLOSED", 0.0, null);
        }

        // 2. GPS distance check
        double distance = haversineDistance(
                session.getFacultyLatitude(), session.getFacultyLongitude(),
                req.getStudentLatitude(),     req.getStudentLongitude());

        if (distance > session.getRadiusMeters()) {
            return StudentCheckInResponse.fail(
                "You are outside the classroom. You are " + Math.round(distance)
                + "m away. Allowed radius: " + Math.round(session.getRadiusMeters()) + "m.",
                "GPS_OUT_OF_RANGE", distance, null);
        }

        // 3. Face confidence check
        double faceConf = (req.getFaceConfidence() != null) ? req.getFaceConfidence() : 0.0;
        if (faceConf < 70.0) {
            return StudentCheckInResponse.fail(
                "Face mismatch. Confidence: " + faceConf + "%. Minimum required: 70%.",
                "FACE_MISMATCH", distance, faceConf);
        }

        // 4. Find student
        Student student = studentRepo.findById(req.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // 5. Prevent duplicate check-in
        boolean alreadyMarked = attendanceRepo.existsByStudentIdAndTimetableIdAndAttendanceDate(
                student.getId(), req.getTimetableId(), LocalDate.now());
        if (alreadyMarked) {
            return StudentCheckInResponse.fail(
                "Attendance already marked for today.",
                "DUPLICATE", distance, null);
        }

        // 6. Calculate ACI score
        long secondsSinceStart = Duration.between(
                session.getStartedAt(), LocalDateTime.now()).getSeconds();

        double aciScore = aciCalculator.calculate(
                distance, session.getRadiusMeters(),
                faceConf, secondsSinceStart, sessionDurationSec);
        String aciLevel = aciCalculator.classify(aciScore);

        // 7. Save attendance
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setTimetable(session.getTimetable());
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setPresent(true);
        attendance.setManualEntry(false);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setGpsDistanceMeters(distance);
        attendance.setFaceConfidence(faceConf);
        attendance.setAciScore(aciScore);
        attendance.setAciLevel(aciLevel);
        attendanceRepo.save(attendance);

        return new StudentCheckInResponse(true,
                "Attendance marked! ACI Score: " + aciScore + " (" + aciLevel + ")",
                aciScore, aciLevel, distance);
    }

    // ── LIVE ATTENDANCE ────────────────────────────────────────────
    public LiveAttendanceResponse getLiveAttendance(Long timetableId) {
        AttendanceSession session =
                sessionRepo.findByTimetableIdAndActiveTrue(timetableId)
                        .orElseThrow(() -> new RuntimeException("No active session"));

        Long batchId = session.getTimetable().getBatch().getId();
        long totalStudents = studentRepo.countByBatchId(batchId);

        List<Attendance> list = attendanceRepo.findByTimetableIdAndAttendanceDate(
                timetableId, LocalDate.now());

        List<AttendanceDTO> dtoList = list.stream()
                .filter(Attendance::isPresent)
                .map(a -> new AttendanceDTO(
                        a.getId(),
                        a.getStudent().getRollNo(),
                        a.getStudent().getFirstName() + " " +
                        (a.getStudent().getLastName() != null ? a.getStudent().getLastName() : ""),
                        a.getCheckInTime(),
                        a.getAciScore(),
                        a.getAciLevel()))
                .toList();

        return new LiveAttendanceResponse(dtoList, totalStudents);
    }

    // ── MARK PRESENT (Manual) ──────────────────────────────────────
    public void markPresent(Long attendanceId) {
        Attendance attendance = attendanceRepo.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));
        attendance.setPresent(true);
        attendance.setManualEntry(true);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setAciScore(50.0);
        attendance.setAciLevel("MEDIUM");
        attendanceRepo.save(attendance);
    }

    // ── MANUAL ATTENDANCE (Exception) ─────────────────────────────
    public void manualAttendance(Long batchId, Long facultyId,
                                  String subject, LocalDate date) {
        // FIX: DayOfWeek filter removed — makeup classes on any day work now
        Timetable timetable = timetableRepo
                .findByBatchIdAndFacultyIdAndSubject(batchId, facultyId, subject)
                .orElseThrow(() -> new RuntimeException(
                    "Timetable not found. Check subject name, faculty, and batch."));

        List<Student> students = studentRepo.findByBatchId(batchId);
        for (Student s : students) {
            boolean exists = attendanceRepo.existsByStudentIdAndTimetableIdAndAttendanceDate(
                    s.getId(), timetable.getId(), date);
            if (!exists) {
                Attendance a = new Attendance();
                a.setStudent(s);
                a.setTimetable(timetable);
                a.setAttendanceDate(date);
                a.setPresent(false);
                a.setManualEntry(true);
                attendanceRepo.save(a);
            }
        }
    }
}
