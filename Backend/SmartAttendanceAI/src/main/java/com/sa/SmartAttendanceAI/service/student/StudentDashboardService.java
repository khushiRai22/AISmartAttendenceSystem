package com.sa.SmartAttendanceAI.service.student;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.SmartAttendanceAI.dto.ActiveSessionDTO;
import com.sa.SmartAttendanceAI.dto.AttendanceHistoryDTO;
import com.sa.SmartAttendanceAI.dto.StudentDashboardDTO;
import com.sa.SmartAttendanceAI.dto.TodayClassDTO;
import com.sa.SmartAttendanceAI.entity.Attendance;
import com.sa.SmartAttendanceAI.entity.AttendanceSession;
import com.sa.SmartAttendanceAI.entity.Student;
import com.sa.SmartAttendanceAI.entity.Timetable;
import com.sa.SmartAttendanceAI.repository.AttendanceRepository;
import com.sa.SmartAttendanceAI.repository.AttendanceSessionRepository;
import com.sa.SmartAttendanceAI.repository.StudentRepository;
import com.sa.SmartAttendanceAI.repository.TimetableRepository;

@Service
public class StudentDashboardService {

    @Autowired private StudentRepository studentRepo;
    @Autowired private TimetableRepository timetableRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private AttendanceSessionRepository sessionRepo;

    // ── Main Dashboard ──
    public StudentDashboardDTO getDashboard(String email) {

        Student student = studentRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Long batchId = student.getBatch().getId();
        Long studentId = student.getId();

        // Attendance stats
        long total = attendanceRepo.countByStudentId(studentId);
        long present = attendanceRepo.countByStudentIdAndPresentTrue(studentId);
        double percentage = (total > 0) ? Math.round((present * 100.0 / total) * 10.0) / 10.0 : 0.0;

        // Active sessions for student's batch
        List<ActiveSessionDTO> activeSessions = getActiveSessions(studentId, batchId);

        // Today's classes
        List<TodayClassDTO> todayClasses = getTodayClasses(studentId, batchId);

        StudentDashboardDTO dto = new StudentDashboardDTO();
        dto.setStudentName(student.getFirstName() + " " + 
                          (student.getLastName() != null ? student.getLastName() : ""));
        dto.setRollNo(student.getRollNo());
        dto.setBatchName(student.getBatch().getName());
        dto.setTotalClasses(total);
        dto.setPresentClasses(present);
        dto.setAttendancePercentage(percentage);
        dto.setActiveSessions(activeSessions);
        dto.setTodayClasses(todayClasses);

        return dto;
    }

    // ── Active Sessions for student's batch ──
    public List<ActiveSessionDTO> getActiveSessions(Long studentId, Long batchId) {

        List<AttendanceSession> allActive = sessionRepo.findAllByActiveTrue();
        List<ActiveSessionDTO> result = new ArrayList<>();

        for (AttendanceSession session : allActive) {
            Timetable timetable = session.getTimetable();

            // Only show sessions for student's batch
            if (!timetable.getBatch().getId().equals(batchId)) continue;

            boolean alreadyMarked = attendanceRepo
                    .existsByStudentIdAndTimetableIdAndAttendanceDate(
                            studentId, timetable.getId(), LocalDate.now());

            ActiveSessionDTO dto = new ActiveSessionDTO();
            dto.setTimetableId(timetable.getId());
            dto.setSessionId(session.getId());
            dto.setSubject(timetable.getSubject());
            dto.setFacultyName(timetable.getFaculty().getFirstName() + " " +
                              (timetable.getFaculty().getLastName() != null ?
                               timetable.getFaculty().getLastName() : ""));
            dto.setStartTime(timetable.getStartTime());
            dto.setEndTime(timetable.getEndTime());
            dto.setSessionStartedAt(session.getStartedAt());
            dto.setAlreadyMarked(alreadyMarked);

            result.add(dto);
        }
        return result;
    }

    // ── Today's Timetable for student's batch ──
    public List<TodayClassDTO> getTodayClasses(Long studentId, Long batchId) {

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<Timetable> timetables = timetableRepo.findByBatchId(batchId);
        List<TodayClassDTO> result = new ArrayList<>();

        for (Timetable t : timetables) {
            if (!t.getDayOfWeek().equals(today)) continue;

            // Check if session is active
            boolean isLive = sessionRepo.findByTimetableIdAndActiveTrue(t.getId()).isPresent();

            // Check if student already marked
            List<Attendance> todayAttendance = attendanceRepo
                    .findByStudentIdAndDate(studentId, LocalDate.now());

            Attendance matchedAttendance = todayAttendance.stream()
                    .filter(a -> a.getTimetable().getId().equals(t.getId()))
                    .findFirst().orElse(null);

            // Determine status
            String status;
            if (matchedAttendance != null && matchedAttendance.isPresent()) {
                status = "PRESENT";
            } else if (matchedAttendance != null && !matchedAttendance.isPresent()) {
                status = "ABSENT";
            } else if (isLive) {
                status = "LIVE";
            } else {
                LocalTime now = LocalTime.now();
                if (now.isBefore(t.getStartTime())) {
                    status = "UPCOMING";
                } else {
                    status = "DONE";
                }
            }

            TodayClassDTO dto = new TodayClassDTO();
            dto.setTimetableId(t.getId());
            dto.setSubject(t.getSubject());
            dto.setFacultyName(t.getFaculty().getFirstName() + " " +
                              (t.getFaculty().getLastName() != null ?
                               t.getFaculty().getLastName() : ""));
            dto.setStartTime(t.getStartTime());
            dto.setEndTime(t.getEndTime());
            dto.setStatus(status);
            dto.setAciScore(matchedAttendance != null ? matchedAttendance.getAciScore() : null);
            dto.setAciLevel(matchedAttendance != null ? matchedAttendance.getAciLevel() : null);

            result.add(dto);
        }
        return result;
    }

    // ── Attendance History ──
    public List<AttendanceHistoryDTO> getAttendanceHistory(String email) {

        Student student = studentRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Attendance> list = attendanceRepo
                .findByStudentIdOrderByAttendanceDateDesc(student.getId());

        List<AttendanceHistoryDTO> result = new ArrayList<>();
        for (Attendance a : list) {
            AttendanceHistoryDTO dto = new AttendanceHistoryDTO();
            dto.setAttendanceId(a.getId());
            dto.setSubject(a.getTimetable().getSubject());
            dto.setAttendanceDate(a.getAttendanceDate());
            dto.setPresent(a.isPresent());
            dto.setCheckInTime(a.getCheckInTime());
            dto.setAciScore(a.getAciScore());
            dto.setAciLevel(a.getAciLevel());
            dto.setManualEntry(a.isManualEntry());
            result.add(dto);
        }
        return result;
    }
}
