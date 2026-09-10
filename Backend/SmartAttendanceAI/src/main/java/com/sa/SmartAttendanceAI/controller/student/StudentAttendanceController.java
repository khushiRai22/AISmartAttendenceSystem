package com.sa.SmartAttendanceAI.controller.student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sa.SmartAttendanceAI.dto.StudentCheckInRequest;
import com.sa.SmartAttendanceAI.dto.StudentCheckInResponse;
import com.sa.SmartAttendanceAI.service.attendance.AttendanceService;

@RestController
@RequestMapping("/api/student/attendance")
@CrossOrigin
public class StudentAttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * Student marks attendance — GPS + Face + ACI
     * POST /api/student/attendance/check-in
     */
    @PostMapping("/check-in")
    public StudentCheckInResponse checkIn(@RequestBody StudentCheckInRequest req) {
        return attendanceService.studentCheckIn(req);
    }
}
