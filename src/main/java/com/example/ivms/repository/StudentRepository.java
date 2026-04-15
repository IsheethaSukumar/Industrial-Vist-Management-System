package com.example.ivms.repository;

import com.example.ivms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByIv_Id(Long ivId);
}
