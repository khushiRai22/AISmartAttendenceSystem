package com.sa.SmartAttendanceAI.service.admin;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.SmartAttendanceAI.dto.StudentInviteRequest;
import com.sa.SmartAttendanceAI.entity.Batches;
import com.sa.SmartAttendanceAI.entity.RegistrationToken;
import com.sa.SmartAttendanceAI.entity.Student;
import com.sa.SmartAttendanceAI.repository.BatchRepository;
import com.sa.SmartAttendanceAI.repository.RegistrationTokenRepository;
import com.sa.SmartAttendanceAI.repository.StudentRepository;
import com.sa.SmartAttendanceAI.service.mail.MailService;
import com.sa.SmartAttendanceAI.util.TokenGenerator;

@Service
public class StudentInviteService {

    // FIX 1: Read base URL from application.properties — no more hardcoded localhost
    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired private StudentRepository studentRepo;
    @Autowired private RegistrationTokenRepository tokenRepo;
    @Autowired private MailService mailService;
    @Autowired private BatchRepository batchRepo;

    // FIX 2: @Transactional — if email fails, DB changes rollback automatically
    @Transactional
    public void inviteStudent(StudentInviteRequest req) {

        Batches batch = batchRepo.findById(req.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (studentRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Student already invited or registered");
        }

        Student s = new Student();
        s.setFirstName(req.getFirstName());
        s.setLastName(req.getLastName());
        s.setEmail(req.getEmail());
        s.setMobile(req.getMobile());
        s.setStatus("INVITED");
        s.setBatch(batch);
        studentRepo.save(s);

        RegistrationToken token = new RegistrationToken();
        token.setToken(TokenGenerator.generate());
        token.setEmail(req.getEmail());
        token.setExpiryTime(LocalDateTime.now().plusHours(24));
        token.setUsed(false);
        token.setRole("STUDENT");
        tokenRepo.save(token);

        // FIX 1: Use configurable baseUrl instead of hardcoded localhost
        String link = baseUrl + "/register.html?token="
                + token.getToken() + "&role=STUDENT";

        String fullName = req.getFirstName() + " " +
                (req.getLastName() != null ? req.getLastName() : "");

        // FIX 2: Email is sent last — if it throws, @Transactional rolls back DB saves
        mailService.sendHtmlInvite(req.getEmail(), fullName.trim(), link, "STUDENT");
    }
}
