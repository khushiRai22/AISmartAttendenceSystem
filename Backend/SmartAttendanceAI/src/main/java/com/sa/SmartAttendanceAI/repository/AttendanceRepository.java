package com.sa.SmartAttendanceAI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sa.SmartAttendanceAI.entity.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Duplicate check — per day per timetable (used for all attendance)
    boolean existsByStudentIdAndTimetableIdAndAttendanceDate(
            Long studentId, Long timetableId, LocalDate date);

    // Used by TimetableService cascade delete
    List<Attendance> findByTimetableId(Long timetableId);

    // Live attendance list for a timetable on a date
    List<Attendance> findByTimetableIdAndAttendanceDate(
            Long timetableId, LocalDate date);

    // Student history
    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    // Stats
    long countByStudentIdAndPresentTrue(Long studentId);
    long countByStudentId(Long studentId);

    // Daily report
    @Query("SELECT a FROM Attendance a WHERE a.timetable.id = :timetableId AND a.attendanceDate = :date")
    List<Attendance> findByTimetableIdAndDate(
            @Param("timetableId") Long timetableId,
            @Param("date") LocalDate date);

    // Student date-based lookup
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND a.attendanceDate = :date")
    List<Attendance> findByStudentIdAndDate(
            @Param("studentId") Long studentId,
            @Param("date") LocalDate date);
}
