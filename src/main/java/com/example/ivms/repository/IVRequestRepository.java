package com.example.ivms.repository;

import com.example.ivms.entity.IVRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IVRequestRepository extends JpaRepository<IVRequest, Long> {
    List<IVRequest> findByCr_Id(Long crId);
    List<IVRequest> findByCompany_Id(Long companyId);
}
