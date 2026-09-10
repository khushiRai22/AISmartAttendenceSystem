package com.sa.SmartAttendanceAI.service.report;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.SmartAttendanceAI.entity.Attendance;
import com.sa.SmartAttendanceAI.entity.Student;
import com.sa.SmartAttendanceAI.repository.AttendanceRepository;
import com.sa.SmartAttendanceAI.repository.StudentRepository;

@Service
public class AttendanceReportService {

    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private StudentRepository studentRepo;

    // ── Batch Report ──────────────────────────────────────────────
    public List<StudentReportDTO> getBatchReport(Long batchId) {
        List<Student> students = studentRepo.findByBatchId(batchId);
        List<StudentReportDTO> report = new ArrayList<>();

        for (Student s : students) {
            long total   = attendanceRepo.countByStudentId(s.getId());
            long present = attendanceRepo.countByStudentIdAndPresentTrue(s.getId());
            double pct   = (total > 0)
                    ? Math.round((present * 100.0 / total) * 10.0) / 10.0 : 0.0;

            // Average ACI score for this student
            List<Attendance> records = attendanceRepo.findByStudentIdOrderByAttendanceDateDesc(s.getId());
            double avgAci = records.stream()
                    .filter(a -> a.getAciScore() != null)
                    .mapToDouble(Attendance::getAciScore)
                    .average().orElse(0.0);
            avgAci = Math.round(avgAci * 10.0) / 10.0;

            StudentReportDTO dto = new StudentReportDTO();
            dto.setStudentId(s.getId());
            dto.setRollNo(s.getRollNo());
            dto.setName((s.getFirstName() != null ? s.getFirstName() : "")
                    + " " + (s.getLastName() != null ? s.getLastName() : ""));
            dto.setEmail(s.getEmail());
            dto.setTotalClasses(total);
            dto.setPresentClasses(present);
            dto.setAttendancePercentage(pct);
            dto.setAvgAciScore(avgAci);
            dto.setStatus(pct >= 75 ? "SAFE" : pct >= 60 ? "WARNING" : "DETAINED");
            report.add(dto);
        }
        return report;
    }

    // ── Daily Report ──────────────────────────────────────────────
    public List<DailyReportDTO> getDailyReport(Long timetableId, LocalDate date) {
        List<Attendance> list = attendanceRepo.findByTimetableIdAndDate(timetableId, date);
        List<DailyReportDTO> report = new ArrayList<>();

        for (Attendance a : list) {
            DailyReportDTO dto = new DailyReportDTO();
            dto.setStudentId(a.getStudent().getId());
            dto.setRollNo(a.getStudent().getRollNo());
            dto.setName((a.getStudent().getFirstName() != null ? a.getStudent().getFirstName() : "")
                    + " " + (a.getStudent().getLastName() != null ? a.getStudent().getLastName() : ""));
            dto.setPresent(a.isPresent());
            dto.setCheckInTime(a.getCheckInTime() != null
                    ? a.getCheckInTime().toLocalTime().toString() : "—");
            dto.setAciScore(a.getAciScore());
            dto.setAciLevel(a.getAciLevel());
            dto.setGpsDistanceMeters(a.getGpsDistanceMeters());
            dto.setFaceConfidence(a.getFaceConfidence());
            dto.setManualEntry(a.isManualEntry());
            report.add(dto);
        }
        return report;
    }

    // ── FIX 8: Excel Export with ACI columns ─────────────────────
    public byte[] exportBatchReportToExcel(Long batchId) throws Exception {
        List<StudentReportDTO> report = getBatchReport(batchId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row header = sheet.createRow(0);
            String[] columns = {
                "Roll No", "Student Name", "Total Classes",
                "Present", "Absent", "Attendance %",
                "Status", "Avg ACI Score", "Trust Level"  // ← UNIQUE: no competitor has these columns
            };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // Data rows
            int rowNum = 1;
            for (StudentReportDTO dto : report) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getRollNo() != null ? dto.getRollNo() : "");
                row.createCell(1).setCellValue(dto.getName());
                row.createCell(2).setCellValue(dto.getTotalClasses());
                row.createCell(3).setCellValue(dto.getPresentClasses());
                row.createCell(4).setCellValue(dto.getTotalClasses() - dto.getPresentClasses());
                row.createCell(5).setCellValue(dto.getAttendancePercentage() + "%");
                row.createCell(6).setCellValue(dto.getStatus());
                row.createCell(7).setCellValue(dto.getAvgAciScore());
                // Trust level based on avg ACI — unique to this system
                String trustLevel = dto.getAvgAciScore() >= 80 ? "HIGH"
                        : dto.getAvgAciScore() >= 55 ? "MEDIUM"
                        : dto.getAvgAciScore() >= 35 ? "LOW" : "SUSPICIOUS";
                row.createCell(8).setCellValue(trustLevel);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── DTOs ──────────────────────────────────────────────────────

    public static class StudentReportDTO {
        private Long studentId;
        private String rollNo, name, status, email;
        private long totalClasses, presentClasses;
        private double attendancePercentage;
        private double avgAciScore; // NEW: average ACI across all sessions

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long v) { this.studentId = v; }
        public String getRollNo() { return rollNo; }
        public void setRollNo(String v) { this.rollNo = v; }
        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public String getStatus() { return status; }
        public void setStatus(String v) { this.status = v; }
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public long getTotalClasses() { return totalClasses; }
        public void setTotalClasses(long v) { this.totalClasses = v; }
        public long getPresentClasses() { return presentClasses; }
        public void setPresentClasses(long v) { this.presentClasses = v; }
        public double getAttendancePercentage() { return attendancePercentage; }
        public void setAttendancePercentage(double v) { this.attendancePercentage = v; }
        public double getAvgAciScore() { return avgAciScore; }
        public void setAvgAciScore(double v) { this.avgAciScore = v; }
    }

    public static class DailyReportDTO {
        private Long studentId;
        private String rollNo, name, checkInTime, aciLevel;
        private boolean present, manualEntry;
        private Double aciScore, gpsDistanceMeters, faceConfidence;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long v) { this.studentId = v; }
        public String getRollNo() { return rollNo; }
        public void setRollNo(String v) { this.rollNo = v; }
        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public String getCheckInTime() { return checkInTime; }
        public void setCheckInTime(String v) { this.checkInTime = v; }
        public boolean isPresent() { return present; }
        public void setPresent(boolean v) { this.present = v; }
        public Double getAciScore() { return aciScore; }
        public void setAciScore(Double v) { this.aciScore = v; }
        public String getAciLevel() { return aciLevel; }
        public void setAciLevel(String v) { this.aciLevel = v; }
        public Double getGpsDistanceMeters() { return gpsDistanceMeters; }
        public void setGpsDistanceMeters(Double v) { this.gpsDistanceMeters = v; }
        public Double getFaceConfidence() { return faceConfidence; }
        public void setFaceConfidence(Double v) { this.faceConfidence = v; }
        public boolean isManualEntry() { return manualEntry; }
        public void setManualEntry(boolean v) { this.manualEntry = v; }
    }
}
