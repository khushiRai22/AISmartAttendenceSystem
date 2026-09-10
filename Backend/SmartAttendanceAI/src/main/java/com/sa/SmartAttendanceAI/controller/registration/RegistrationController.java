package com.sa.SmartAttendanceAI.controller.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sa.SmartAttendanceAI.service.RegistrationService.RegistrationService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public String register(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String firstName,
            @RequestParam(required = false) String middleName,
            @RequestParam String lastName,
            @RequestParam String dob,
            @RequestParam String gender,
            @RequestParam String mobile,
            @RequestParam(required = false) String rollNo,

            // ✅ FACULTY FIELDS
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) String joiningDate,

            @RequestParam String password,
            @RequestPart("profilePic") MultipartFile profilePic
    ) throws Exception {

        registrationService.register(
                token, title, firstName, middleName,
                lastName, dob, gender, mobile,
                rollNo,
                employeeId, department, designation,
                experience, joiningDate,
                password, profilePic
        );

        return "Registration Successful";
    }
}