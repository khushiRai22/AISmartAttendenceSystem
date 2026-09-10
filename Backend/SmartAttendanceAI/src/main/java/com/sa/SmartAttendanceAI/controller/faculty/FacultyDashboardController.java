package com.sa.SmartAttendanceAI.controller.faculty;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.sa.SmartAttendanceAI.dto.FacultyTimetableDTO;
import com.sa.SmartAttendanceAI.entity.Faculty;
import com.sa.SmartAttendanceAI.entity.Timetable;
import com.sa.SmartAttendanceAI.repository.FacultyRepository;
import com.sa.SmartAttendanceAI.repository.TimetableRepository;

@RestController
@RequestMapping("/api/faculty")
@CrossOrigin
public class FacultyDashboardController {

    @Autowired private TimetableRepository timetableRepository;
    @Autowired private FacultyRepository facultyRepository;

    // GET /api/faculty/timetable
    @GetMapping("/timetable")
    public List<FacultyTimetableDTO> getMyTimetable() {
        Faculty faculty = getLoggedInFaculty();
        return timetableRepository.findByFacultyId(faculty.getId())
                .stream()
                .map(this::toTimetableDTO)
                .collect(Collectors.toList());
    }

    // GET /api/faculty/me
    @GetMapping("/me")
    public Faculty getLoggedInFaculty() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return facultyRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
    }

    /**
     * GET /api/faculty/batches
     * Returns unique batches from this faculty's timetable
     * Used by Reports tab to populate batch dropdown
     */
    @GetMapping("/batches")
    public List<Map<String, Object>> getMyBatches() {
        Faculty faculty = getLoggedInFaculty();
        List<Timetable> timetables = timetableRepository.findByFacultyId(faculty.getId());

        // Deduplicate by batch ID
        Map<Long, Map<String, Object>> batchMap = new LinkedHashMap<>();
        for (Timetable t : timetables) {
            if (t.getBatch() != null) {
                Long batchId = t.getBatch().getId();
                if (!batchMap.containsKey(batchId)) {
                    Map<String, Object> b = new LinkedHashMap<>();
                    b.put("id", batchId);
                    b.put("name", t.getBatch().getName());
                    batchMap.put(batchId, b);
                }
            }
        }
        return new ArrayList<>(batchMap.values());
    }

    /**
     * GET /api/faculty/timetables-by-batch/{batchId}
     * Returns timetables for a specific batch (for daily report dropdown)
     */
    @GetMapping("/timetables-by-batch/{batchId}")
    public List<Map<String, Object>> getTimetablesByBatch(@PathVariable Long batchId) {
        Faculty faculty = getLoggedInFaculty();
        List<Timetable> timetables = timetableRepository.findByFacultyId(faculty.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Timetable t : timetables) {
            if (t.getBatch() != null && t.getBatch().getId().equals(batchId)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", t.getId());
                item.put("subject", t.getSubject());
                item.put("dayOfWeek", t.getDayOfWeek());
                item.put("startTime", t.getStartTime());
                item.put("endTime", t.getEndTime());
                result.add(item);
            }
        }
        return result;
    }

    private FacultyTimetableDTO toTimetableDTO(Timetable timetable) {
        FacultyTimetableDTO dto = new FacultyTimetableDTO();
        dto.setId(timetable.getId());
        dto.setSubject(timetable.getSubject());
        dto.setDayOfWeek(timetable.getDayOfWeek());
        dto.setStartTime(timetable.getStartTime());
        dto.setEndTime(timetable.getEndTime());
        dto.setClassroomName(timetable.getClassroomName());
        dto.setActive(timetable.isActive());
        dto.setAttendanceStarted(timetable.isAttendanceStarted());

        if (timetable.getBatch() != null) {
            dto.setBatchId(timetable.getBatch().getId());
            dto.setBatchName(timetable.getBatch().getName());
        }

        if (timetable.getFaculty() != null) {
            dto.setFacultyId(timetable.getFaculty().getId());
            String firstName = timetable.getFaculty().getFirstName() != null
                    ? timetable.getFaculty().getFirstName() : "";
            String lastName = timetable.getFaculty().getLastName() != null
                    ? " " + timetable.getFaculty().getLastName() : "";
            dto.setFacultyName((firstName + lastName).trim());
        }

        return dto;
    }
}
