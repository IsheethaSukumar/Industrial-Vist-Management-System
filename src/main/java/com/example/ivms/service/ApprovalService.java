package com.example.ivms.service;

import com.example.ivms.entity.Approval;
import com.example.ivms.entity.IVRequest;
import com.example.ivms.repository.ApprovalRepository;
import com.example.ivms.repository.IVRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApprovalService {
    private final ApprovalRepository approvalRepository;
    private final IVRequestRepository ivRequestRepository;

    public ApprovalService(ApprovalRepository approvalRepository, IVRequestRepository ivRequestRepository) {
        this.approvalRepository = approvalRepository;
        this.ivRequestRepository = ivRequestRepository;
    }

    public Approval createPending(Long ivId, String role) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        Approval a = new Approval();
        a.setIv(o.get());
        a.setRole(role);
        a.setStatus("PENDING");
        return approvalRepository.save(a);
    }

    public Approval act(Long approvalId, String approver, String status, String remarks) {
        Optional<Approval> o = approvalRepository.findById(approvalId);
        if (o.isEmpty()) return null;
        Approval a = o.get();
        a.setApproverName(approver);
        a.setStatus(status);
        a.setRemarks(remarks);
        a.setApprovalDate(LocalDateTime.now());
        Approval saved = approvalRepository.save(a);
        // also reflect on IVRequest status
        IVRequest iv = saved.getIv();
        if (iv != null) {
            if ("APPROVED".equalsIgnoreCase(status)) {
                iv.setStatus("APPROVED");
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                iv.setStatus("REJECTED");
            }
            ivRequestRepository.save(iv);
        }
        return saved;
    }

    public List<Approval> findAll() { return approvalRepository.findAll(); }

    public List<Approval> findPendingByRole(String role) {
        return approvalRepository.findByRoleAndStatus(role, "PENDING");
    }

    public List<Approval> findByRoleAndStatus(String role, String status) {
        return approvalRepository.findByRoleAndStatus(role, status);
    }

    public List<Approval> findByIv(Long ivId) {
        return approvalRepository.findByIv_Id(ivId);
    }

    public Optional<Approval> getById(Long id) {
        return approvalRepository.findById(id);
    }

    public Approval getOrCreateHodForIv(Long ivId) {
        // find existing HOD pending/any for this IV
        var list = approvalRepository.findByIv_Id(ivId);
        for (var a : list) {
            if ("HOD".equalsIgnoreCase(a.getRole())) return a;
        }
        return createPending(ivId, "HOD");
    }
}
