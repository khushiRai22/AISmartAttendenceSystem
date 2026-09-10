package com.sa.SmartAttendanceAI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.sa.SmartAttendanceAI.entity.AttendanceSession;

public interface AttendanceSessionRepository
        extends JpaRepository<AttendanceSession, Long> {

    Optional<AttendanceSession> findByTimetableIdAndActiveTrue(Long timetableId);

    List<AttendanceSession> findAllByActiveTrue();

    // Used by TimetableService cascade delete
    List<AttendanceSession> findByTimetableId(Long timetableId);
}
