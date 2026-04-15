package com.example.ivms.repository;

import com.example.ivms.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    java.util.List<Approval> findByRoleAndStatus(String role, String status);
    java.util.List<Approval> findByStatus(String status);
    java.util.List<Approval> findByIv_Id(Long ivId);
}
