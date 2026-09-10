package com.sa.SmartAttendanceAI.service.alert;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sa.SmartAttendanceAI.entity.Batches;
import com.sa.SmartAttendanceAI.repository.BatchRepository;
import com.sa.SmartAttendanceAI.service.mail.MailService;
import com.sa.SmartAttendanceAI.service.report.AttendanceReportService;
import com.sa.SmartAttendanceAI.service.report.AttendanceReportService.StudentReportDTO;

/**
 * FIX 9: Low Attendance Alert Service
 * Previously: DETAINED status was computed but email was never sent
 * Now: Every Monday 8 AM — auto-emails all students below 75%
 */
@Service
public class LowAttendanceAlertService {

    @Autowired private AttendanceReportService reportService;
    @Autowired private BatchRepository batchRepo;
    @Autowired private MailService mailService;

    // Runs every Monday at 8:00 AM automatically
    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklyLowAttendanceAlerts() {
        System.out.println("📧 Running weekly low-attendance alert job...");

        List<Batches> allBatches = batchRepo.findAll();
        int alertsSent = 0;

        for (Batches batch : allBatches) {
            List<StudentReportDTO> report = reportService.getBatchReport(batch.getId());

            for (StudentReportDTO student : report) {
                if (student.getAttendancePercentage() < 75.0 && student.getEmail() != null) {
                    try {
                        mailService.sendLowAttendanceAlert(
                                student.getEmail(),
                                student.getName(),
                                student.getAttendancePercentage(),
                                student.getStatus()
                        );
                        alertsSent++;
                    } catch (Exception e) {
                        System.err.println("Failed to send alert to " + student.getEmail() + ": " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("✅ Low-attendance alerts sent: " + alertsSent);
    }
}
