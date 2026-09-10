package com.sa.SmartAttendanceAI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.SmartAttendanceAI.entity.Student;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);
    
    List<Student> findByBatchId(Long batchId);
    
    long countByBatchId(Long batchId);
}

