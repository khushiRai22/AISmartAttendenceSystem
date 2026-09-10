package com.sa.SmartAttendanceAI.controller.faculty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.sa.SmartAttendanceAI.dto.LiveAttendanceResponse;
import com.sa.SmartAttendanceAI.dto.StartSessionRequest;
import com.sa.SmartAttendanceAI.entity.AttendanceSession;
import com.sa.SmartAttendanceAI.entity.Faculty;
import com.sa.SmartAttendanceAI.repository.AttendanceSessionRepository;
import com.sa.SmartAttendanceAI.repository.FacultyRepository;
import com.sa.SmartAttendanceAI.service.attendance.AttendanceService;

@RestController
@RequestMapping("/api/faculty/attendance")
@CrossOrigin
public class FacultyAttendanceController {

    @Autowired private AttendanceService service;
    @Autowired private AttendanceSessionRepository sessionRepo;
    @Autowired private FacultyRepository facultyRepo;

    /**
     * POST /api/faculty/attendance/start-session
     * Now auto-closes old session if exists — no more "Session already active" error
     */
    @PostMapping("/start-session")
    public ResponseEntity<String> startSession(@RequestBody StartSessionRequest req) {
        try {
            req.setFacultyId(getLoggedInFaculty().getId());
            service.startSession(req);
            return ResponseEntity.ok("Attendance Session Started");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/faculty/attendance/end-session
     */
    @PostMapping("/end-session")
    public ResponseEntity<String> endSession(@RequestParam Long timetableId) {
        try {
            service.endSession(timetableId);
            return ResponseEntity.ok("Session Ended");
        } catch (Exception e) {
            // Even if no active session found, return OK — frontend treats it as done
            return ResponseEntity.ok("Session already closed");
        }
    }

    /**
     * GET /api/faculty/attendance/live/{timetableId}
     */
    @GetMapping("/live/{timetableId}")
    public ResponseEntity<?> getLiveAttendance(@PathVariable Long timetableId) {
        try {
            return ResponseEntity.ok(service.getLiveAttendance(timetableId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/faculty/attendance/session-status?timetableId=X
     * Frontend uses this on page reload to recover active session
     */
    @GetMapping("/session-status")
    public ResponseEntity<Map<String, Object>> getSessionStatus(
            @RequestParam Long timetableId) {
        Map<String, Object> result = new HashMap<>();
        sessionRepo.findByTimetableIdAndActiveTrue(timetableId).ifPresentOrElse(
            s -> {
                result.put("active", true);
                result.put("sessionId", s.getId());
                result.put("startedAt", s.getStartedAt());
                result.put("radiusMeters", s.getRadiusMeters());
            },
            () -> result.put("active", false)
        );
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/faculty/attendance/manual
     */
    @PostMapping("/manual")
    public ResponseEntity<String> manualAttendance(
            @RequestParam Long batchId,
            @RequestParam String subject,
            @RequestParam String date) {
        try {
            service.manualAttendance(
                    batchId,
                    getLoggedInFaculty().getId(),
                    subject,
                    LocalDate.parse(date));
            return ResponseEntity.ok("Manual attendance created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Faculty getLoggedInFaculty() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return facultyRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
    }
}
