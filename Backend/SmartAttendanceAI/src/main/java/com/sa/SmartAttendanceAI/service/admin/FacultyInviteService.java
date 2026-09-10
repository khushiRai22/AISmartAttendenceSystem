package com.sa.SmartAttendanceAI.service.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.SmartAttendanceAI.dto.FacultyInviteRequest;
import com.sa.SmartAttendanceAI.entity.Faculty;
import com.sa.SmartAttendanceAI.entity.RegistrationToken;
import com.sa.SmartAttendanceAI.repository.AttendanceRepository;
import com.sa.SmartAttendanceAI.repository.AttendanceSessionRepository;
import com.sa.SmartAttendanceAI.repository.FacultyRepository;
import com.sa.SmartAttendanceAI.repository.RegistrationTokenRepository;
import com.sa.SmartAttendanceAI.repository.TimetableRepository;
import com.sa.SmartAttendanceAI.repository.UserRepository;
import com.sa.SmartAttendanceAI.service.mail.MailService;
import com.sa.SmartAttendanceAI.util.TokenGenerator;

@Service
public class FacultyInviteService {

    // FIX 1: Read base URL from application.properties
    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired private FacultyRepository facultyRepo;
    @Autowired private RegistrationTokenRepository tokenRepo;
    @Autowired private MailService mailService;
    @Autowired private TimetableRepository timetableRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private AttendanceSessionRepository sessionRepo;
    @Autowired private UserRepository userRepo;

    // FIX 2: @Transactional — if email fails, DB rolls back
    @Transactional
    public void inviteFaculty(FacultyInviteRequest req) {

        if (facultyRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Faculty already invited or exists");
        }

        Faculty f = new Faculty();
        f.setFirstName(req.getFirstName());
        f.setLastName(req.getLastName());
        f.setEmail(req.getEmail());
        f.setStatus("INVITED");
        facultyRepo.save(f);

        RegistrationToken token = new RegistrationToken();
        token.setToken(TokenGenerator.generate());
        token.setEmail(req.getEmail());
        token.setExpiryTime(LocalDateTime.now().plusHours(24));
        token.setUsed(false);
        token.setRole("FACULTY");
        tokenRepo.save(token);

        // FIX 1: Use configurable baseUrl
        String link = baseUrl + "/register.html?token="
                + token.getToken() + "&role=FACULTY";

        String fullName = req.getFirstName() + " " +
                (req.getLastName() != null ? req.getLastName() : "");

        mailService.sendHtmlInvite(req.getEmail(), fullName.trim(), link, "FACULTY");
    }

    public List<Faculty> getAllFaculty() {
        return facultyRepo.findAll();
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = facultyRepo.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Faculty not found"));

        // Step 1 — Faculty ના timetables find
        List<com.sa.SmartAttendanceAI.entity.Timetable>
            timetables = timetableRepo.findByFacultyId(id);

        for (com.sa.SmartAttendanceAI.entity.Timetable t
                : timetables) {
            // Step 2 — Each timetable ની attendance delete
            List<com.sa.SmartAttendanceAI.entity.Attendance>
                attendances = attendanceRepo
                    .findByTimetableId(t.getId());
            if (!attendances.isEmpty()) {
                attendanceRepo.deleteAll(attendances);
            }
            // Step 3 — Sessions delete
            List<com.sa.SmartAttendanceAI.entity.AttendanceSession>
                sessions = sessionRepo
                    .findByTimetableId(t.getId());
            if (!sessions.isEmpty()) {
                sessionRepo.deleteAll(sessions);
            }
            // Step 4 — Timetable delete
            timetableRepo.deleteById(t.getId());
        }

        // Step 5 — Token delete
        if (faculty.getEmail() != null) {
            tokenRepo.deleteByEmail(faculty.getEmail());
            userRepo.deleteByEmail(faculty.getEmail());
        }

        // Step 6 — Faculty delete
        facultyRepo.deleteById(id);
    }
}
