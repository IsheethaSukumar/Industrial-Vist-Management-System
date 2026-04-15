package com.example.ivms.service;

import com.example.ivms.entity.*;
import com.example.ivms.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IVRequestService {
    private final IVRequestRepository ivRequestRepository;
    private final CompanyRepository companyRepository;
    private final StudentRepository studentRepository;
    private final ApprovalService approvalService;

    public IVRequestService(IVRequestRepository ivRequestRepository, CompanyRepository companyRepository, StudentRepository studentRepository, ApprovalService approvalService) {
        this.ivRequestRepository = ivRequestRepository;
        this.companyRepository = companyRepository;
        this.studentRepository = studentRepository;
        this.approvalService = approvalService;
    }

    public IVRequest create(CR cr, Long companyId) {
        Optional<Company> c = companyRepository.findById(companyId);
        if (c.isEmpty()) return null;
        IVRequest iv = new IVRequest();
        iv.setCr(cr);
        iv.setCompany(c.get());
        iv.setStatus("CREATED");
        return ivRequestRepository.save(iv);
    }

    public IVRequest inviteStaff(Long ivId, String invited) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setInvitedStaff(invited);
        return ivRequestRepository.save(iv);
    }

    public IVRequest setTentative(Long ivId, LocalDateTime dt) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setTentativeDateTime(dt);
        return ivRequestRepository.save(iv);
    }

    public IVRequest submitForApproval(Long ivId) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setStatus("APPROVAL_IN_PROGRESS");
        IVRequest saved = ivRequestRepository.save(iv);
        // Ensure a pending HOD approval exists
        approvalService.createPending(saved.getId(), "HOD");
        return saved;
    }

    public IVRequest uploadHRConfirmation(Long ivId, String path) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setHrConfirmationPath(path);
        return ivRequestRepository.save(iv);
    }

    public IVRequest markCompleted(Long ivId, String attendedNotes) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setStatus("COMPLETED");
        // Reuse hrConfirmationPath to store attended notes/details for now
        if (attendedNotes != null) {
            iv.setHrConfirmationPath(attendedNotes);
        }
        return ivRequestRepository.save(iv);
    }

    public IVRequest setTransport(Long ivId, String transportDetails) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setTransportDetails(transportDetails);
        return ivRequestRepository.save(iv);
    }

    public IVRequest setStudentLimit(Long ivId, Integer limit) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setStudentLimit(limit);
        return ivRequestRepository.save(iv);
    }

    public List<Student> addStudents(Long ivId, List<Student> students) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return List.of();
        IVRequest iv = o.get();
        for (Student s : students) {
            s.setIv(iv);
            studentRepository.save(s);
        }
        return studentRepository.findAll();
    }

    public Optional<IVRequest> getById(Long id) { return ivRequestRepository.findById(id); }
    public List<IVRequest> findAll() { return ivRequestRepository.findAll(); }
}
