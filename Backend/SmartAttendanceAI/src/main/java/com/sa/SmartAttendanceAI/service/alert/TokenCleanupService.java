package com.sa.SmartAttendanceAI.service.alert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.SmartAttendanceAI.repository.RegistrationTokenRepository;

import java.time.LocalDateTime;

/**
 * FIX 10: Token Cleanup Service
 * Previously: Expired registration tokens accumulated in DB forever
 * Now: Every day at midnight — expired unused tokens are auto-deleted
 */
@Service
public class TokenCleanupService {

    @Autowired private RegistrationTokenRepository tokenRepo;

    // Runs every day at midnight
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredTokens() {
        tokenRepo.deleteByExpiryTimeBefore(LocalDateTime.now());
        System.out.println("✅ Expired registration tokens cleaned up");
    }
}
