package com.sa.SmartAttendanceAI.controller.report;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sa.SmartAttendanceAI.service.report.AttendanceReportService;
import com.sa.SmartAttendanceAI.service.report.AttendanceReportService.DailyReportDTO;
import com.sa.SmartAttendanceAI.service.report.AttendanceReportService.StudentReportDTO;

@RestController
@RequestMapping("/api/faculty/reports")
@CrossOrigin
public class AttendanceReportController {

    @Autowired private AttendanceReportService reportService;

    // Batch report — JSON (for dashboard display)
    @GetMapping("/batch/{batchId}")
    public List<StudentReportDTO> getBatchReport(@PathVariable Long batchId) {
        return reportService.getBatchReport(batchId);
    }

    // Daily report — JSON (for daily view)
    @GetMapping("/daily/{timetableId}")
    public List<DailyReportDTO> getDailyReport(
            @PathVariable Long timetableId,
            @RequestParam(required = false) String date) {
        LocalDate reportDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        return reportService.getDailyReport(timetableId, reportDate);
    }

    // FIX 8: Excel export — downloads .xlsx file with ACI columns
    @GetMapping("/batch/{batchId}/export")
    public ResponseEntity<byte[]> exportBatchReport(@PathVariable Long batchId) throws Exception {
        byte[] excelBytes = reportService.exportBatchReportToExcel(batchId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment",
                "attendance_report_batch_" + batchId + ".xlsx");
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}
