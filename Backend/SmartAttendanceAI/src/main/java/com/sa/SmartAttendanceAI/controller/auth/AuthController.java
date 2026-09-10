package com.sa.SmartAttendanceAI.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.SmartAttendanceAI.dto.LoginRequest;
import com.sa.SmartAttendanceAI.dto.LoginResponse;
import com.sa.SmartAttendanceAI.service.auth.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
