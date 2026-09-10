package com.sa.SmartAttendanceAI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.SmartAttendanceAI.entity.Faculty;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
