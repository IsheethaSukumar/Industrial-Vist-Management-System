package com.example.ivms.repository;

import com.example.ivms.entity.CR;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CRRepository extends JpaRepository<CR, Long> {
    Optional<CR> findByRollNoAndPassword(String rollNo, String password);
    Optional<CR> findByRollNo(String rollNo);
}
