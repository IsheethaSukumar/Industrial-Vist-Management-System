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

    // New Workflow Methods
    public IVRequest createProposal(CR cr, String dept, String yrSec, Integer numStudents, String purpose, String prefDomain) {
        IVRequest iv = new IVRequest();
        iv.setCr(cr);
        iv.setDepartment(dept);
        iv.setYearAndSection(yrSec);
        iv.setNumberOfStudents(numStudents);
        iv.setPurpose(purpose);
        iv.setPreferredDomain(prefDomain);
        iv.setStatus("SUBMITTED");
        IVRequest saved = ivRequestRepository.save(iv);
        approvalService.createPending(saved.getId(), "INITIAL_HOD");
        return saved;
    }

    public IVRequest initialHodApproval(Long ivId, String action, String remarks) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        if ("APPROVE".equalsIgnoreCase(action)) {
            iv.setStatus("COMPANY_SELECTION");
        } else {
            iv.setStatus("INITIAL_REJECTED");
        }
        ivRequestRepository.save(iv);
        var approvals = approvalService.findByIv(ivId);
        for (var a : approvals) {
            if ("INITIAL_HOD".equalsIgnoreCase(a.getRole()) && "PENDING".equalsIgnoreCase(a.getStatus())) {
                approvalService.act(a.getId(), "HOD", "APPROVE".equalsIgnoreCase(action) ? "APPROVED" : "REJECTED", remarks);
            }
        }
        return iv;
    }

    public IVRequest selectCompanyAndDates(Long ivId, Long companyId, LocalDateTime tentativeDate, LocalDateTime alternateDate, Integer finalStrength, String companyRequestMessage) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        Optional<Company> c = companyRepository.findById(companyId);
        if (c.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setCompany(c.get());
        iv.setTentativeDateTime(tentativeDate);
        iv.setAlternateDate(alternateDate);
        iv.setStudentLimit(finalStrength);
        iv.setCompanyRequestMessage(companyRequestMessage);
        iv.setStatus("COMPANY_REQUESTED");
        return ivRequestRepository.save(iv);
    }

    public IVRequest companyAction(Long ivId, String action, LocalDateTime alternateDate) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        if ("ACCEPT".equalsIgnoreCase(action)) {
            iv.setStatus("COMPANY_ACCEPTED");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            iv.setStatus("COMPANY_REJECTED");
        } else if ("SUGGEST".equalsIgnoreCase(action) && alternateDate != null) {
            iv.setTentativeDateTime(alternateDate);
            iv.setStatus("COMPANY_ACCEPTED");
        }
        return ivRequestRepository.save(iv);
    }

    public IVRequest uploadAcknowledgement(Long ivId, String hrConfirmationPath, String studentsPdfPath, String facultyName) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setHrConfirmationPath(hrConfirmationPath);
        iv.setStudentsPdfPath(studentsPdfPath);
        iv.setFacultyInCharge(facultyName);
        iv.setFacultyStatus("PENDING");
        iv.setStatus("FACULTY_ASSIGNED");
        return ivRequestRepository.save(iv);
    }

    public IVRequest finalHodApproval(Long ivId, String action, String remarks) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        if ("APPROVE".equalsIgnoreCase(action)) {
            iv.setStatus("COMPANY_CONFIRMED");
        } else {
            iv.setStatus("FINAL_REJECTED");
        }
        ivRequestRepository.save(iv);
        var approvals = approvalService.findByIv(ivId);
        for (var a : approvals) {
            if ("FINAL_HOD".equalsIgnoreCase(a.getRole()) && "PENDING".equalsIgnoreCase(a.getStatus())) {
                approvalService.act(a.getId(), "HOD", "APPROVE".equalsIgnoreCase(action) ? "APPROVED" : "REJECTED", remarks);
            }
        }
        return iv;
    }

    public IVRequest assignFaculty(Long ivId, String facultyName) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setFacultyInCharge(facultyName);
        iv.setFacultyStatus("PENDING");
        iv.setStatus("FACULTY_ASSIGNED");
        return ivRequestRepository.save(iv);
    }

    public IVRequest facultyAction(Long ivId, String action) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        if ("ACCEPT".equalsIgnoreCase(action)) {
            iv.setFacultyStatus("ACCEPTED");
            iv.setStatus("DATE_STRENGTH_SUBMITTED");
            approvalService.createPending(iv.getId(), "FINAL_HOD");
        } else {
            iv.setFacultyStatus("DECLINED");
            iv.setStatus("COMPANY_ACCEPTED");
        }
        return ivRequestRepository.save(iv);
    }

    public IVRequest planTransport(Long ivId, Integer busCount, String busAllocation, String driverDetails, String pickupLocation, LocalDateTime departureTime, LocalDateTime returnTime, Double transportCost) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setBusCount(busCount);
        iv.setBusAllocation(busAllocation);
        iv.setDriverDetails(driverDetails);
        iv.setPickupLocation(pickupLocation);
        iv.setDepartureTime(departureTime);
        iv.setReturnTime(returnTime);
        iv.setTransportCost(transportCost);
        iv.setStatus("TRANSPORT_PLANNED");
        return ivRequestRepository.save(iv);
    }

    public IVRequest planFood(Long ivId, Boolean breakfast, Boolean lunch, Boolean snacks, Boolean water, Double foodCost) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setFoodBreakfast(breakfast);
        iv.setFoodLunch(lunch);
        iv.setFoodSnacks(snacks);
        iv.setFoodWater(water);
        iv.setFoodCost(foodCost);
        iv.setStatus("FOOD_PLANNED");
        return ivRequestRepository.save(iv);
    }

    public IVRequest planBudget(Long ivId, Double entryFees, Double miscExpenses) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setEntryFees(entryFees);
        iv.setMiscExpenses(miscExpenses);
        double total = 0.0;
        if (iv.getTransportCost() != null) total += iv.getTransportCost();
        if (iv.getFoodCost() != null) total += iv.getFoodCost();
        if (entryFees != null) total += entryFees;
        if (miscExpenses != null) total += miscExpenses;
        iv.setTotalBudget(total);
        iv.setStatus("BUDGET_PREPARED");
        return ivRequestRepository.save(iv);
    }

    public IVRequest departmentalApproval(Long ivId, String action, String remarks) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        if ("APPROVE".equalsIgnoreCase(action)) {
            iv.setStatus("CONDUCTED");
        } else {
            iv.setStatus("BUDGET_PREPARED");
        }
        return ivRequestRepository.save(iv);
    }

    public IVRequest uploadProof(Long ivId, String photosPath, String attendancePath, String reportPath, String feedback) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setProofPhotosPath(photosPath);
        iv.setProofAttendancePath(attendancePath);
        iv.setProofReportPath(reportPath);
        iv.setProofFeedback(feedback);
        iv.setStatus("PROOF_UPLOADED");
        return ivRequestRepository.save(iv);
    }

    public IVRequest hodVerifyProof(Long ivId, String action) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        if ("APPROVE".equalsIgnoreCase(action)) {
            iv.setStatus("COMPLETED");
        } else {
            iv.setStatus("CONDUCTED");
        }
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
