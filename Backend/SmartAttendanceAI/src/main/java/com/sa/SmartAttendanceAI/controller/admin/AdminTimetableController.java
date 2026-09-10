package com.sa.SmartAttendanceAI.controller.admin;

import com.sa.SmartAttendanceAI.dto.CreateTimetableRequest;
import com.sa.SmartAttendanceAI.entity.Timetable;
import com.sa.SmartAttendanceAI.service.admin.TimetableService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/timetables")
@CrossOrigin
public class AdminTimetableController {

    private final TimetableService service;

    public AdminTimetableController(TimetableService service) {
        this.service = service;
    }

    @PostMapping
    public Timetable create(@RequestBody CreateTimetableRequest request) {
        return service.createTimetable(request);
    }

    @GetMapping
    public List<Timetable> getAll() {
        return service.getAllTimetables();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteTimetable(id);
    }
}