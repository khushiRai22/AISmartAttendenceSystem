package com.sa.SmartAttendanceAI.service.mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    // Read sender email from application.properties
    @Value("${spring.mail.username}")
    private String senderEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Invite email
    public void sendHtmlInvite(String toEmail, String name,
                               String registrationLink, String role) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email.html");
            String htmlTemplate = new String(
                    resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String safeRole = (role != null) ? role.toUpperCase() : "USER";
            String safeUuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            String finalHtml = htmlTemplate
                    .replace("{{name}}", name != null ? name : "")
                    .replace("{{link}}", registrationLink != null ? registrationLink : "")
                    .replace("{{role}}", safeRole)
                    .replace("{{uuid}}", safeUuid);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // FIX: Use actual Gmail account as sender — fake domain not allowed by Gmail SMTP
            helper.setFrom(senderEmail, "SmartAttendance Team");
            helper.setTo(toEmail.trim()); // trim() removes accidental spaces
            helper.setSubject("SmartAttendance.AI | Your Registration Invite");
            helper.setText(finalHtml, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send invite email: " + e.getMessage());
        }
    }

    // Low attendance alert email
    public void sendLowAttendanceAlert(String toEmail, String name,
                                        double percentage, String status) {
        try {
            String color = status.equals("DETAINED") ? "#E24B4A" : "#EF9F27";
            String emoji = status.equals("DETAINED") ? "🚨" : "⚠️";

            String html = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f8fafc;padding:20px'>"
                + "<div style='max-width:500px;margin:0 auto;background:#fff;border-radius:16px;padding:30px;border:1px solid #e2e8f0'>"
                + "<h2 style='color:#0f172a;margin-bottom:8px'>SmartAttendance<span style='color:#0ea5e9'>AI</span></h2>"
                + "<p style='color:#64748b;font-size:14px'>Attendance Alert</p>"
                + "<hr style='border:none;border-top:1px solid #e2e8f0;margin:20px 0'>"
                + "<p style='font-size:16px;color:#0f172a'>Dear <strong>" + name + "</strong>,</p>"
                + "<p style='color:#334155'>Your current attendance is:</p>"
                + "<div style='background:#f8fafc;border-radius:12px;padding:20px;text-align:center;margin:20px 0'>"
                + "<span style='font-size:42px;font-weight:700;color:" + color + "'>" + percentage + "%</span><br>"
                + "<span style='font-size:14px;font-weight:600;color:" + color + ";background:" + color + "22;padding:4px 12px;border-radius:20px'>"
                + emoji + " " + status + "</span>"
                + "</div>"
                + (status.equals("DETAINED")
                    ? "<p style='color:#E24B4A;font-size:13px'>Your attendance has fallen below 60%. You are at risk of <strong>detention from examinations</strong>. Please attend all upcoming classes immediately.</p>"
                    : "<p style='color:#854F0B;font-size:13px'>Your attendance is below 75%. Please attend more classes to avoid detention risk.</p>")
                + "<p style='color:#94a3b8;font-size:12px;margin-top:20px'>This is an automated alert from SmartAttendanceAI.</p>"
                + "</div></body></html>";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail, "SmartAttendance Team");
            helper.setTo(toEmail.trim());
            helper.setSubject(emoji + " SmartAttendance.AI | Attendance Alert — " + percentage + "%");
            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send alert to " + toEmail + ": " + e.getMessage());
        }
    }
}
