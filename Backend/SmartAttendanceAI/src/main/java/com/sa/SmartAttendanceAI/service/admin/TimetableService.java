package com.sa.SmartAttendanceAI.service.admin;

import com.sa.SmartAttendanceAI.dto.CreateTimetableRequest;
import com.sa.SmartAttendanceAI.entity.*;
import com.sa.SmartAttendanceAI.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
public class TimetableService {

    private final TimetableRepository timetableRepo;
    private final BatchRepository batchRepo;
    private final FacultyRepository facultyRepo;
    private final AttendanceRepository attendanceRepo;
    private final AttendanceSessionRepository sessionRepo;

    public TimetableService(TimetableRepository timetableRepo,
                            BatchRepository batchRepo,
                            FacultyRepository facultyRepo,
                            AttendanceRepository attendanceRepo,
                            AttendanceSessionRepository sessionRepo) {
        this.timetableRepo  = timetableRepo;
        this.batchRepo      = batchRepo;
        this.facultyRepo    = facultyRepo;
        this.attendanceRepo = attendanceRepo;
        this.sessionRepo    = sessionRepo;
    }

    public Timetable createTimetable(CreateTimetableRequest request) {
        Batches batch = batchRepo.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        Faculty faculty = facultyRepo.findById(request.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        DayOfWeek day       = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime   = LocalTime.parse(request.getEndTime());

        // FIX 7: Conflict detection — same batch cannot have two classes at the same time
        boolean conflict = timetableRepo.findByBatchId(request.getBatchId())
                .stream()
                .filter(t -> t.getDayOfWeek().equals(day))
                .anyMatch(t ->
                        !endTime.isBefore(t.getStartTime()) &&
                        !startTime.isAfter(t.getEndTime())
                );
        if (conflict) {
            throw new RuntimeException(
                "Time conflict: This batch already has a class scheduled in this time slot on "
                + day + ". Please choose a different time.");
        }

        Timetable timetable = new Timetable();
        timetable.setBatch(batch);
        timetable.setFaculty(faculty);
        timetable.setSubject(request.getSubject());
        timetable.setDayOfWeek(day);
        timetable.setStartTime(startTime);
        timetable.setEndTime(endTime);
        timetable.setClassroomName(request.getClassroomName());
        return timetableRepo.save(timetable);
    }

    public List<Timetable> getAllTimetables() {
        return timetableRepo.findAll();
    }

    /**
     * Cascade delete — order matters for FK constraints:
     * Step 1 → Delete Attendance records
     * Step 2 → Delete AttendanceSession records
     * Step 3 → Delete Timetable
     */
    @Transactional
    public void deleteTimetable(Long id) {
        List<Attendance> attendances = attendanceRepo.findByTimetableId(id);
        if (!attendances.isEmpty()) attendanceRepo.deleteAll(attendances);

        List<AttendanceSession> sessions = sessionRepo.findByTimetableId(id);
        if (!sessions.isEmpty()) sessionRepo.deleteAll(sessions);

        timetableRepo.deleteById(id);
    }
}
