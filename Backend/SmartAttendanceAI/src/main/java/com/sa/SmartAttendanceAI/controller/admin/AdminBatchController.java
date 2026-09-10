package com.sa.SmartAttendanceAI.controller.admin;

import com.sa.SmartAttendanceAI.dto.CreateBatchRequest;
import com.sa.SmartAttendanceAI.entity.Batches;
import com.sa.SmartAttendanceAI.service.admin.BatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/batches")
@CrossOrigin
public class AdminBatchController {

    private final BatchService batchService;

    public AdminBatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    public Batches createBatch(@RequestBody CreateBatchRequest request) {
        return batchService.createBatch(request);
    }

    @GetMapping
    public List<Batches> getAllBatches() {
        return batchService.getAllBatches();
    }

    @DeleteMapping("/{id}")
    public void deleteBatch(@PathVariable Long id) {
        batchService.deleteBatch(id);
    }
}