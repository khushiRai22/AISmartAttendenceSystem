package com.sa.SmartAttendanceAI.service.admin;

import com.sa.SmartAttendanceAI.dto.CreateBatchRequest;
import com.sa.SmartAttendanceAI.entity.Batches;
import com.sa.SmartAttendanceAI.repository.BatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchService {

    private final BatchRepository batchRepository;

    public BatchService(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public Batches createBatch(CreateBatchRequest request) {

        String generatedName =
                request.getDepartment() + "-" +
                request.getYear() + "-" +
                request.getSection();

        if(batchRepository.existsByName(generatedName)) {
            throw new RuntimeException("Batch already exists");
        }

        Batches batch = new Batches();
        batch.setName(generatedName);
        batch.setDepartment(request.getDepartment());
        batch.setYear(request.getYear());
        batch.setSection(request.getSection());

        return batchRepository.save(batch);
    }

    public List<Batches> getAllBatches() {
        return batchRepository.findAll();
    }

    public void deleteBatch(Long id) {
        batchRepository.deleteById(id);
    }
}