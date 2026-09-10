package com.sa.SmartAttendanceAI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// FIX 9: @EnableScheduling needed for LowAttendanceAlertService @Scheduled to work
@SpringBootApplication
@EnableScheduling
public class SmartAttendanceAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartAttendanceAiApplication.class, args);
    }
}
