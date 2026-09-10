package com.sa.SmartAttendanceAI.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.SmartAttendanceAI.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long>{
	//Java 8 Feature 
	Optional<User> findByEmail(String email);

	void deleteByEmail(String email);
}
