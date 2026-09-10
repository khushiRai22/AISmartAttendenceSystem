package com.sa.SmartAttendanceAI.repository;

import com.sa.SmartAttendanceAI.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    List<Timetable> findByFacultyId(Long facultyId);

    List<Timetable> findByBatchId(Long batchId);

    List<Timetable> findByDayOfWeek(DayOfWeek dayOfWeek);

    // Used by auto check-in (strict — day must match)
    Optional<Timetable> findByBatchIdAndFacultyIdAndSubjectAndDayOfWeek(
            Long batchId,
            Long facultyId,
            String subject,
            DayOfWeek dayOfWeek);

    // Used by manual attendance (flexible — day not required, e.g. makeup class)
    Optional<Timetable> findByBatchIdAndFacultyIdAndSubject(
            Long batchId,
            Long facultyId,
            String subject);
}