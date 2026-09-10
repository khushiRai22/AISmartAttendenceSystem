package com.sa.SmartAttendanceAI.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.sa.SmartAttendanceAI.entity.User;
import com.sa.SmartAttendanceAI.repository.UserRepository;

@Configuration
public class DataInitializers implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@smartattendance.com";

        // Check if admin already exists
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            System.out.println("Admin already exists....,\n skipping creation......");
            return;
        }

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("admin@123"));
        admin.setRole("ADMIN");
        admin.setEnabled(true);

        userRepository.save(admin);
        System.out.println("==================================");
        System.out.println("Default ADMIN created successfully");
        System.out.println("Email: admin@smartattendance.com");
        System.out.println("Password: admin@123");
        System.out.println("==================================");
    }
}