package com.example.ivms.controller;

import com.example.ivms.entity.CR;
import com.example.ivms.entity.Company;
import com.example.ivms.entity.IVRequest;
import com.example.ivms.repository.CompanyRepository;
import com.example.ivms.repository.IVRequestRepository;
import com.example.ivms.repository.CRRepository;
import com.example.ivms.repository.StudentRepository;
import com.example.ivms.service.CRService;
import com.example.ivms.service.IVRequestService;
import com.example.ivms.service.ApprovalService;
import com.example.ivms.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/web")
public class WebController {

    private final CRService crService;
    private final CRRepository crRepository;
    private final CompanyRepository companyRepository;
    private final IVRequestRepository ivRequestRepository;
    private final IVRequestService ivRequestService;
    private final ApprovalService approvalService;
    private final CompanyService companyService;
    private final StudentRepository studentRepository;

    public WebController(CRService crService, CRRepository crRepository, CompanyRepository companyRepository, IVRequestService ivRequestService, ApprovalService approvalService, CompanyService companyService, IVRequestRepository ivRequestRepository, StudentRepository studentRepository) {
        this.crService = crService;
        this.crRepository = crRepository;
        this.companyRepository = companyRepository;
        this.ivRequestService = ivRequestService;
        this.approvalService = approvalService;
        this.companyService = companyService;
        this.ivRequestRepository = ivRequestRepository;
        this.studentRepository = studentRepository;
    }

    // Home shortcuts
    @GetMapping("/")
    public String home() {
        return "redirect:/";
    }

    // CR register form
    @GetMapping("/cr/register")
    public String crRegisterForm(Model model) {
        model.addAttribute("cr", new CR());
        return "cr/register";
    }

    @PostMapping("/cr/register")
    public String crRegisterSubmit(@ModelAttribute CR cr, Model model) {
        CR saved = crService.register(cr);
        model.addAttribute("message", "CR registered with id: " + saved.getId());
        return "result";
    }

    // CR login form
    @GetMapping("/cr/login")
    public String crLoginForm() {
        return "cr/login";
    }

    @PostMapping("/cr/login")
    public String crLogin(@RequestParam String rollNo, @RequestParam String password, Model model) {
        Optional<CR> cr = crService.login(rollNo, password);
        if (cr.isPresent()) {
            model.addAttribute("cr", cr.get());
            return "cr/dashboard";
        } else {
            model.addAttribute("message", "Invalid credentials. Use Register if you are new.");
            return "result";
        }
    }

    // Company create form
    @GetMapping("/company/new")
    public String companyNewForm(Model model) {
        model.addAttribute("company", new Company());
        return "company/new";
    }

    @PostMapping("/company")
    public String companyCreate(@ModelAttribute Company company, Model model) {
        // Upsert by HR Email: update existing if present
        Company saved = companyRepository.findByHrEmail(company.getHrEmail())
                .map(ex -> {
                    ex.setName(company.getName());
                    ex.setLocation(company.getLocation());
                    ex.setHrName(company.getHrName());
                    ex.setContactNo(company.getContactNo());
                    return companyRepository.save(ex);
                })
                .orElseGet(() -> companyRepository.save(company));
        model.addAttribute("message", "Company saved with id: " + saved.getId());
        return "result";
    }

    // IV form
    @GetMapping("/iv/full-new")
    public String ivFullNew(Model model) {
        List<CR> crs = crRepository.findAll();
        List<Company> companies = companyRepository.findAll();
        model.addAttribute("crs", crs);
        model.addAttribute("companies", companies);
        return "iv/full_new";
    }

    @PostMapping("/iv/full-create")
    public String ivFullCreate(@RequestParam Long crId,
                               @RequestParam Long companyId,
                               @RequestParam(required = false) String invitedStaff,
                               @RequestParam(required = false) String dateTime,
                               @RequestParam(required = false) String transportDetails,
                               @RequestParam(required = false) String studentsText,
                               Model model) {
        Optional<CR> cr = crRepository.findById(crId);
        if (cr.isEmpty()) { model.addAttribute("message", "CR not found"); return "result"; }
        IVRequest iv = ivRequestService.create(cr.get(), companyId);
        if (iv == null) { model.addAttribute("message", "Company not found"); return "result"; }
        if (invitedStaff != null) { ivRequestService.inviteStaff(iv.getId(), invitedStaff); }
        if (dateTime != null && !dateTime.isBlank()) { ivRequestService.setTentative(iv.getId(), java.time.LocalDateTime.parse(dateTime)); }
        if (transportDetails != null) { ivRequestService.setTransport(iv.getId(), transportDetails); }
        // parse students
        if (studentsText != null && !studentsText.isBlank()) {
            java.util.List<com.example.ivms.entity.Student> list = new java.util.ArrayList<>();
            for (String line : studentsText.split("\n")) {
                String t = line.trim(); if (t.isEmpty()) continue;
                String[] parts = t.split(",");
                com.example.ivms.entity.Student s = new com.example.ivms.entity.Student();
                if (parts.length > 0) s.setName(parts[0].trim());
                if (parts.length > 1) s.setRollNo(parts[1].trim());
                if (parts.length > 2) s.setDept(parts[2].trim());
                list.add(s);
            }
            ivRequestService.addStudents(iv.getId(), list);
        }
        model.addAttribute("message", "IV created/updated with id: " + iv.getId());
        return "result";
    }



    // Company actions page
    @GetMapping("/company/actions")
    public String companyActionsForm() { return "company/actions"; }

    @PostMapping("/company/confirm-date")
    public String companyConfirm(@RequestParam Long ivId, @RequestParam String dateTime, Model model) {
        var iv = companyService.approveOrSuggest(ivId, java.time.LocalDateTime.parse(dateTime));
        model.addAttribute("message", iv == null ? "IV not found" : ("Company confirmed date for IV " + iv.getId()));
        return "result";
    }

    @PostMapping("/company/student-limit")
    public String companyLimit(@RequestParam Long ivId, @RequestParam Integer limit, Model model) {
        var iv = ivRequestService.setStudentLimit(ivId, limit);
        model.addAttribute("message", iv == null ? "IV not found" : ("Student limit set to " + limit + " for IV " + iv.getId()));
        return "result";
    }


    // Company list page
    @GetMapping("/company")
    public String companyList(Model model) {
        List<Company> companies = companyRepository.findAll();
        model.addAttribute("companies", companies);
        return "company/list";
    }

    // CR: View my IVs and their statuses
    @GetMapping("/cr/{crId}/ivs")
    public String crIvs(@PathVariable Long crId, Model model) {
        var list = ivRequestRepository.findByCr_Id(crId);
        model.addAttribute("crId", crId);
        model.addAttribute("ivs", list);
        return "cr/iv_list";
    }

    // Company login
    @GetMapping("/company/login")
    public String companyLoginForm() { return "company/login"; }

    @PostMapping("/company/login")
    public String companyLogin(@RequestParam String hrEmail, Model model) {
        var c = companyRepository.findByHrEmail(hrEmail);
        if (c.isPresent()) {
            model.addAttribute("company", c.get());
            return "company/dashboard";
        }
        model.addAttribute("message", "Company not found. Please create/update company first.");
        return "result";
    }

    // Company: view IVs for this company
    @GetMapping("/company/{companyId}/ivs")
    public String companyIvs(@PathVariable Long companyId, Model model) {
        var list = ivRequestRepository.findByCompany_Id(companyId);
        model.addAttribute("companyId", companyId);
        model.addAttribute("ivs", list);
        return "company/iv_list";
    }

    // Company: IV details
    @GetMapping("/company/iv/{ivId}")
    public String companyIvDetails(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        var students = studentRepository.findByIv_Id(ivId);
        model.addAttribute("iv", iv.get());
        model.addAttribute("students", students);
        return "company/iv_details";
    }
    // HOD login
    @GetMapping("/hod/login")
    public String hodLoginForm() { return "hod/login"; }

    @PostMapping("/hod/login")
    public String hodLogin(@RequestParam String name, Model model) {
        model.addAttribute("hodName", name);
        return "hod/dashboard";
    }

    // HOD: list pending approvals
    @GetMapping("/hod/approvals")
    public String hodApprovals(Model model) {
        var pendings = approvalService.findPendingByRole("HOD");
        model.addAttribute("approvals", pendings);
        model.addAttribute("filter", "PENDING");
        return "hod/approvals";
    }

    // HOD: list approved approvals
    @GetMapping("/hod/approvals/approved")
    public String hodApprovalsApproved(Model model) {
        var list = approvalService.findByRoleAndStatus("HOD", "APPROVED");
        model.addAttribute("approvals", list);
        model.addAttribute("filter", "APPROVED");
        return "hod/approvals";
    }

    // HOD: list rejected approvals
    @GetMapping("/hod/approvals/rejected")
    public String hodApprovalsRejected(Model model) {
        var list = approvalService.findByRoleAndStatus("HOD", "REJECTED");
        model.addAttribute("approvals", list);
        model.addAttribute("filter", "REJECTED");
        return "hod/approvals";
    }

    // HOD: approval details
    @GetMapping("/hod/approval/{id}")
    public String hodApprovalDetails(@PathVariable Long id, Model model) {
        var a = approvalService.getById(id);
        if (a.isEmpty()) { model.addAttribute("message", "Approval not found"); return "result"; }
        model.addAttribute("approval", a.get());
        return "hod/approval_details";
    }

    // HOD: view IV details by ID and get/create HOD approval
    @GetMapping("/hod/iv/{ivId}")
    public String hodIvReview(@PathVariable Long ivId, Model model) {
        var ivOpt = ivRequestService.getById(ivId);
        if (ivOpt.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        var iv = ivOpt.get();
        var approval = approvalService.getOrCreateHodForIv(ivId);
        model.addAttribute("iv", iv);
        model.addAttribute("approval", approval);
        return "hod/iv_review";
    }

    // HOD: mark an IV as completed with optional notes
    @PostMapping("/hod/iv/{ivId}/complete")
    public String hodIvComplete(@PathVariable Long ivId,
                                @RequestParam(required = false, name = "notes") String notes,
                                Model model) {
        var iv = ivRequestService.markCompleted(ivId, notes);
        model.addAttribute("message", iv == null ? "IV not found" : ("Marked IV " + iv.getId() + " as COMPLETED"));
        return "result";
    }

    // HOD: view all IV requests
    @GetMapping("/hod/ivs")
    public String hodIvs(Model model) {
        var list = ivRequestService.findAll();
        model.addAttribute("ivs", list);
        return "hod/iv_list";
    }

    // CR: company visited summary with student counts
    @GetMapping("/cr/{crId}/visited")
    public String crVisited(@PathVariable Long crId, Model model) {
        var all = ivRequestRepository.findByCr_Id(crId);
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        for (var iv : all) {
            if (iv.getStatus() != null && (iv.getStatus().equalsIgnoreCase("COMPLETED") || iv.getStatus().equalsIgnoreCase("APPROVED"))) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("ivId", iv.getId());
                m.put("company", iv.getCompany() != null ? iv.getCompany().getName() : "—");
                m.put("dateTime", iv.getTentativeDateTime());
                m.put("students", studentRepository.findByIv_Id(iv.getId()).size());
                rows.add(m);
            }
        }
        model.addAttribute("crId", crId);
        model.addAttribute("rows", rows);
        return "cr/visited";
    }

    // CR: IV details with student list
    @GetMapping("/cr/iv/{ivId}")
    public String crIvDetails(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        var students = studentRepository.findByIv_Id(ivId);
        model.addAttribute("iv", iv.get());
        model.addAttribute("students", students);
        return "cr/iv_details";
    }

    // CR: submit IV for approval (creates HOD approval stage)
    @PostMapping("/cr/iv/{ivId}/submit")
    public String crIvSubmit(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.submitForApproval(ivId);
        model.addAttribute("message", iv == null ? "IV not found" : ("Submitted IV " + iv.getId() + " for HOD approval"));
        return "result";
    }
}
