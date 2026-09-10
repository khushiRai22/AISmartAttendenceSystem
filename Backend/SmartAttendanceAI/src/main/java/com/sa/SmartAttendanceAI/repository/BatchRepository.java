package com.sa.SmartAttendanceAI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sa.SmartAttendanceAI.entity.Batches;

import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batches, Long> {

    Optional<Batches> findByName(String name);

    boolean existsByName(String name);
}