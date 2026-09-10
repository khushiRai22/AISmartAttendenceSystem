package com.sa.SmartAttendanceAI.service.RegistrationService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sa.SmartAttendanceAI.entity.*;
import com.sa.SmartAttendanceAI.repository.*;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationTokenRepository tokenRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private FacultyRepository facultyRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public void register(
            String tokenStr,
            String title,
            String firstName,
            String middleName,
            String lastName,
            String dob,
            String gender,
            String mobile,
            String rollNo,

            // ✅ FACULTY FIELDS
            String employeeId,
            String department,
            String designation,
            Integer experience,
            String joiningDate,

            String password,
            MultipartFile profilePic
    ) throws Exception {

        // 🔐 1️⃣ Validate Token
    	RegistrationToken token = tokenRepo.findByToken(tokenStr)
    	        .orElseThrow(() -> new RuntimeException("Invalid Token"));

    	String role = token.getRole();   // ✅ SAFE

        if (token.isUsed())
            throw new RuntimeException("Token already used");

        if (token.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Token expired");

        if (profilePic == null || profilePic.isEmpty())
            throw new RuntimeException("Profile photo is required");

        byte[] imageBytes = profilePic.getBytes();
        String email = token.getEmail();

        // 🔐 Encode Password
        String encodedPassword = passwordEncoder.encode(password);

        // ❗ Prevent duplicate user creation
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already registered");
        }

        if ("STUDENT".equalsIgnoreCase(role)) {

            Student student = studentRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            student.setTitle(title);
            student.setFirstName(firstName);
            student.setMiddleName(middleName);
            student.setLastName(lastName);
            student.setDob(LocalDate.parse(dob));
            student.setGender(gender);
            student.setMobile(mobile);
            student.setRollNo(rollNo);
            student.setProfilePic(imageBytes);
            student.setStatus("ACTIVE");

            studentRepo.save(student);

        }  else if ("FACULTY".equalsIgnoreCase(role)) {

            Faculty faculty = facultyRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Faculty not found"));

            faculty.setTitle(title);
            faculty.setFirstName(firstName);
            faculty.setMiddleName(middleName);
            faculty.setLastName(lastName);
            faculty.setGender(gender);
            faculty.setMobile(mobile);
            faculty.setProfilePic(imageBytes);
            faculty.setStatus("ACTIVE");

            // ✅ NEW FIELDS
            faculty.setEmployeeId(employeeId);
            faculty.setDepartment(department);
            faculty.setDesignation(designation);
            faculty.setExperience(experience);

            if (joiningDate != null && !joiningDate.isEmpty()) {
                faculty.setJoiningDate(LocalDate.parse(joiningDate));
            }

            facultyRepo.save(faculty);
        }else {
            throw new RuntimeException("Invalid Role");
        }

        // ✅ CREATE USER LOGIN ENTRY (THIS WAS MISSING)
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(role.toUpperCase());  // STUDENT / FACULTY
        user.setEnabled(true);

        userRepository.save(user);

        // 🔐 Mark token used
        token.setUsed(true);
        tokenRepo.save(token);
    }
}

