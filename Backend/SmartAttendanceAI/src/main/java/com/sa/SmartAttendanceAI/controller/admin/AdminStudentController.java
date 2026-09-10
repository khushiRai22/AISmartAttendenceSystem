package com.sa.SmartAttendanceAI.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.SmartAttendanceAI.dto.StudentInviteRequest;
import com.sa.SmartAttendanceAI.entity.Student;
import com.sa.SmartAttendanceAI.repository.AttendanceRepository;
import com.sa.SmartAttendanceAI.repository.RegistrationTokenRepository;
import com.sa.SmartAttendanceAI.repository.StudentRepository;
import com.sa.SmartAttendanceAI.service.admin.StudentInviteService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    @Autowired private StudentInviteService service;
    @Autowired private StudentRepository studentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private RegistrationTokenRepository tokenRepository;

    @PostMapping("/invite")
    public String invite(@RequestBody StudentInviteRequest req) {
        service.inviteStudent(req);
        return "Registration link sent";
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deleteStudent(@PathVariable Long id) {

        Student student = studentRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Student not found"));

        // Step 1 — Attendance records delete
        List<com.sa.SmartAttendanceAI.entity.Attendance>
            attendances = attendanceRepository
                .findByStudentIdOrderByAttendanceDateDesc(id);
        if (!attendances.isEmpty()) {
            attendanceRepository.deleteAll(attendances);
        }

        // Step 2 — Registration token delete
        if (student.getEmail() != null) {
            tokenRepository.deleteByEmail(student.getEmail());
        }

        // Step 3 — Student delete
        studentRepository.deleteById(id);
    }
}
