package com.sa.SmartAttendanceAI.controller.student;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.sa.SmartAttendanceAI.dto.ActiveSessionDTO;
import com.sa.SmartAttendanceAI.dto.AttendanceHistoryDTO;
import com.sa.SmartAttendanceAI.dto.StudentDashboardDTO;
import com.sa.SmartAttendanceAI.dto.StudentProfileDTO;
import com.sa.SmartAttendanceAI.dto.TimetableDTO;
import com.sa.SmartAttendanceAI.dto.TodayClassDTO;
import com.sa.SmartAttendanceAI.entity.Student;
import com.sa.SmartAttendanceAI.entity.Timetable;
import com.sa.SmartAttendanceAI.repository.StudentRepository;
import com.sa.SmartAttendanceAI.repository.TimetableRepository;
import com.sa.SmartAttendanceAI.service.student.StudentDashboardService;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentDashboardController {

    @Autowired private StudentDashboardService dashboardService;
    @Autowired private StudentRepository studentRepo;
    @Autowired private TimetableRepository timetableRepo;

    /** GET /api/student/dashboard */
    @GetMapping("/dashboard")
    public StudentDashboardDTO getDashboard() {
        return dashboardService.getDashboard(getEmail());
    }

    /** GET /api/student/sessions/active */
    @GetMapping("/sessions/active")
    public List<ActiveSessionDTO> getActiveSessions() {
        Student student = getStudent();
        return dashboardService.getActiveSessions(
                student.getId(), student.getBatch().getId());
    }

    /** GET /api/student/timetable — returns TimetableDTO list with facultyName */
    @GetMapping("/timetable")
    public List<TimetableDTO> getMyTimetable() {
        Student student = getStudent();
        List<Timetable> list = timetableRepo.findByBatchId(student.getBatch().getId());

        return list.stream().map(t -> {
            TimetableDTO dto = new TimetableDTO();
            dto.setId(t.getId());
            dto.setSubject(t.getSubject());
            dto.setDayOfWeek(t.getDayOfWeek());
            dto.setStartTime(t.getStartTime());
            dto.setEndTime(t.getEndTime());
            dto.setBatchName(t.getBatch().getName());
            if (t.getFaculty() != null) {
                String fname = t.getFaculty().getFirstName()
                        + (t.getFaculty().getLastName() != null
                           ? " " + t.getFaculty().getLastName() : "");
                dto.setFacultyName(fname);
            }
            dto.setType("Theory"); // default
            return dto;
        }).collect(Collectors.toList());
    }

    /** GET /api/student/classes/today */
    @GetMapping("/classes/today")
    public List<TodayClassDTO> getTodayClasses() {
        Student student = getStudent();
        return dashboardService.getTodayClasses(
                student.getId(), student.getBatch().getId());
    }

    /** GET /api/student/attendance/history */
    @GetMapping("/attendance/history")
    public List<AttendanceHistoryDTO> getAttendanceHistory() {
        return dashboardService.getAttendanceHistory(getEmail());
    }

    /** GET /api/student/me */
    @GetMapping("/me")
    public StudentProfileDTO getProfile() {
        Student s = getStudent();
        StudentProfileDTO dto = new StudentProfileDTO();
        dto.setId(s.getId());
        dto.setFirstName(s.getFirstName());
        dto.setLastName(s.getLastName());
        dto.setEmail(s.getEmail());
        dto.setMobile(s.getMobile());
        dto.setGender(s.getGender());
        dto.setDob(s.getDob());
        dto.setRollNo(s.getRollNo());
        dto.setStatus(s.getStatus());
        if (s.getBatch() != null) {
            dto.setBatchName(s.getBatch().getName());
            dto.setBatchId(s.getBatch().getId());
        }
        return dto;
    }

    private String getEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Student getStudent() {
        return studentRepo.findByEmail(getEmail())
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }
}
