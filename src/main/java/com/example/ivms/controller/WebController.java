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
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import jakarta.servlet.http.HttpServletResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Element;
import java.awt.Color;

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
            return "redirect:/web/cr/" + cr.get().getId() + "/dashboard";
        } else {
            model.addAttribute("message", "Invalid credentials. Use Register if you are new.");
            return "result";
        }
    }

    @GetMapping("/cr/{crId}/dashboard")
    public String crDashboard(@PathVariable Long crId, Model model) {
        var cr = crRepository.findById(crId);
        if (cr.isEmpty()) { model.addAttribute("message", "CR not found"); return "result"; }
        model.addAttribute("cr", cr.get());
        return "cr/dashboard";
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
            return "redirect:/web/company/" + c.get().getId() + "/dashboard";
        }
        model.addAttribute("message", "Company not found. Please create/update company first.");
        return "result";
    }

    @GetMapping("/company/{companyId}/dashboard")
    public String companyDashboard(@PathVariable Long companyId, Model model) {
        var c = companyRepository.findById(companyId);
        if (c.isEmpty()) { model.addAttribute("message", "Company not found"); return "result"; }
        model.addAttribute("company", c.get());
        return "company/dashboard";
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
        return "redirect:/web/hod/" + name + "/dashboard";
    }

    @GetMapping("/hod/{name}/dashboard")
    public String hodDashboard(@PathVariable String name, Model model) {
        model.addAttribute("hodName", name);
        return "hod/dashboard";
    }

    // HOD: list pending approvals
    @GetMapping("/hod/approvals")
    public String hodApprovals(Model model) {
        var pendings = new java.util.ArrayList<com.example.ivms.entity.Approval>();
        pendings.addAll(approvalService.findPendingByRole("HOD"));
        pendings.addAll(approvalService.findPendingByRole("INITIAL_HOD"));
        pendings.addAll(approvalService.findPendingByRole("FINAL_HOD"));
        model.addAttribute("approvals", pendings);
        model.addAttribute("filter", "PENDING");
        return "hod/approvals";
    }

    // HOD: list approved approvals
    @GetMapping("/hod/approvals/approved")
    public String hodApprovalsApproved(Model model) {
        var list = new java.util.ArrayList<com.example.ivms.entity.Approval>();
        list.addAll(approvalService.findByRoleAndStatus("HOD", "APPROVED"));
        list.addAll(approvalService.findByRoleAndStatus("INITIAL_HOD", "APPROVED"));
        list.addAll(approvalService.findByRoleAndStatus("FINAL_HOD", "APPROVED"));
        model.addAttribute("approvals", list);
        model.addAttribute("filter", "APPROVED");
        return "hod/approvals";
    }

    // HOD: list rejected approvals
    @GetMapping("/hod/approvals/rejected")
    public String hodApprovalsRejected(Model model) {
        var list = new java.util.ArrayList<com.example.ivms.entity.Approval>();
        list.addAll(approvalService.findByRoleAndStatus("HOD", "REJECTED"));
        list.addAll(approvalService.findByRoleAndStatus("INITIAL_HOD", "REJECTED"));
        list.addAll(approvalService.findByRoleAndStatus("FINAL_HOD", "REJECTED"));
        model.addAttribute("approvals", list);
        model.addAttribute("filter", "REJECTED");
        return "hod/approvals";
    }

    // HOD: approval details
    @GetMapping("/hod/approval/{id}")
    public String hodApprovalDetails(@PathVariable Long id, Model model) {
        var a = approvalService.getById(id);
        if (a.isEmpty()) { model.addAttribute("message", "Approval not found"); return "result"; }
        var approval = a.get();
        if ("INITIAL_HOD".equalsIgnoreCase(approval.getRole())) {
            return "redirect:/web/hod/proposal/" + approval.getIv().getId();
        } else if ("FINAL_HOD".equalsIgnoreCase(approval.getRole())) {
            return "redirect:/web/hod/iv/" + approval.getIv().getId() + "/final-review";
        }
        model.addAttribute("approval", approval);
        return "hod/approval_details";
    }

    @PostMapping("/staff/approve")
    public String staffApprove(@RequestParam Long approvalId,
                               @RequestParam String approverName,
                               @RequestParam String status,
                               @RequestParam(required = false) String remarks,
                               Model model) {
        var app = approvalService.act(approvalId, approverName, status, remarks);
        model.addAttribute("message", app == null ? "Approval not found" : "Decision submitted. Status: " + app.getStatus());
        return "result";
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

    // New Workflow Endpoints
    @GetMapping("/cr/{crId}/proposal/new")
    public String newProposalForm(@PathVariable Long crId, Model model) {
        model.addAttribute("crId", crId);
        return "cr/new_proposal";
    }

    @PostMapping("/cr/proposal")
    public String submitProposal(@RequestParam Long crId,
                                 @RequestParam String department,
                                 @RequestParam String yearAndSection,
                                 @RequestParam Integer numberOfStudents,
                                 @RequestParam String purpose,
                                 @RequestParam String preferredDomain,
                                 Model model) {
        var cr = crRepository.findById(crId);
        if (cr.isEmpty()) { model.addAttribute("message", "CR not found"); return "result"; }
        var iv = ivRequestService.createProposal(cr.get(), department, yearAndSection, numberOfStudents, purpose, preferredDomain);
        model.addAttribute("message", "Proposal submitted successfully! IV ID: " + iv.getId());
        return "result";
    }

    @GetMapping("/hod/proposal/{ivId}")
    public String initialReviewForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "hod/proposal_review";
    }

    @PostMapping("/hod/proposal/{ivId}/initial-approve")
    public String initialApprove(@PathVariable Long ivId,
                                 @RequestParam String action,
                                 @RequestParam String remarks,
                                 Model model) {
        var iv = ivRequestService.initialHodApproval(ivId, action, remarks);
        model.addAttribute("message", iv == null ? "IV not found" : "Initial proposal " + action + "D with remarks.");
        return "result";
    }

    @GetMapping("/cr/iv/{ivId}/companies")
    public String filterCompaniesForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        var companies = companyRepository.findAll();
        model.addAttribute("iv", iv.get());
        model.addAttribute("companies", companies);
        return "cr/company_filter";
    }

    @PostMapping("/cr/iv/{ivId}/select-company")
    public String selectCompanyAndDates(@PathVariable Long ivId,
                                        @RequestParam Long companyId,
                                        @RequestParam String tentativeDate,
                                        @RequestParam(required = false) String alternateDate,
                                        @RequestParam Integer finalStrength,
                                        @RequestParam(required = false) String companyRequestMessage,
                                        Model model) {
        var tDt = java.time.LocalDateTime.parse(tentativeDate);
        java.time.LocalDateTime aDt = (alternateDate != null && !alternateDate.isBlank()) ? java.time.LocalDateTime.parse(alternateDate) : null;
        var iv = ivRequestService.selectCompanyAndDates(ivId, companyId, tDt, aDt, finalStrength, companyRequestMessage);
        model.addAttribute("message", iv == null ? "Failed to select company" : "Company request submitted successfully.");
        return "result";
    }

    @GetMapping("/cr/iv/{ivId}/acknowledgement")
    public String viewAcknowledgementForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "cr/acknowledgement";
    }

    @PostMapping("/cr/iv/{ivId}/acknowledgement/upload")
    public String uploadAcknowledgement(@PathVariable Long ivId,
                                        @RequestParam("hrConfirmationFile") MultipartFile hrConfirmationFile,
                                        @RequestParam("studentsPdfFile") MultipartFile studentsPdfFile,
                                        @RequestParam String facultyName,
                                        Model model) {
        String uploadDirStr = "src/main/resources/static/uploads/";
        File uploadDir = new File(uploadDirStr);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String hrFilename = "ack_" + ivId + "_" + hrConfirmationFile.getOriginalFilename();
        String studentsFilename = "students_" + ivId + "_" + studentsPdfFile.getOriginalFilename();
        try {
            hrConfirmationFile.transferTo(new File(uploadDir.getAbsolutePath() + File.separator + hrFilename));
            studentsPdfFile.transferTo(new File(uploadDir.getAbsolutePath() + File.separator + studentsFilename));
        } catch (IOException e) {
            model.addAttribute("message", "Error uploading files: " + e.getMessage());
            return "result";
        }

        String hrPath = "/uploads/" + hrFilename;
        String studentsPath = "/uploads/" + studentsFilename;

        var iv = ivRequestService.uploadAcknowledgement(ivId, hrPath, studentsPath, facultyName);
        model.addAttribute("message", iv == null ? "Failed to process request" : "Acknowledgement and student list uploaded successfully, and faculty approval request sent!");
        return "result";
    }

    @GetMapping("/cr/iv/{ivId}/acknowledgement/download")
    public void downloadAcknowledgementPdf(@PathVariable Long ivId, HttpServletResponse response) {
        var ivOpt = ivRequestService.getById(ivId);
        if (ivOpt.isEmpty()) return;
        var iv = ivOpt.get();
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"acknowledgement_iv_" + ivId + ".pdf\"");
        
        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            
            document.add(new Paragraph("INDUSTRIAL VISIT ACKNOWLEDGEMENT", titleFont));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("This is an official document generated by the Industrial Visit Management System (IVMS).", normalFont));
            document.add(new Paragraph("\n"));
            
            document.add(new Paragraph("IV Request Reference details:", headerFont));
            document.add(new Paragraph("IV ID: " + iv.getId(), normalFont));
            document.add(new Paragraph("Company Name: " + (iv.getCompany() != null ? iv.getCompany().getName() : "—"), normalFont));
            document.add(new Paragraph("Location: " + (iv.getCompany() != null ? iv.getCompany().getLocation() : "—"), normalFont));
            document.add(new Paragraph("Proposed Date/Time: " + iv.getTentativeDateTime(), normalFont));
            document.add(new Paragraph("Student Strength Limit: " + iv.getStudentLimit(), normalFont));
            document.add(new Paragraph("\n"));
            
            document.add(new Paragraph("Acknowledgement Statement:", headerFont));
            document.add(new Paragraph("\"We hereby acknowledge that the company " + (iv.getCompany() != null ? iv.getCompany().getName() : "—") 
                + " has officially accepted our request for an Industrial Visit scheduled on " + iv.getTentativeDateTime() 
                + " for a batch of " + iv.getStudentLimit() + " students.\"", normalFont));
            document.add(new Paragraph("\n"));
            
            var students = studentRepository.findByIv_Id(ivId);
            document.add(new Paragraph("Registered Students List (" + students.size() + "):", headerFont));
            document.add(new Paragraph("\n"));
            
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Paragraph("Student Name", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Roll Number", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Department", headerFont)));
            
            for (var s : students) {
                table.addCell(new PdfPCell(new Paragraph(s.getName(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(s.getRollNo(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(s.getDept(), normalFont)));
            }
            document.add(table);
            
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/cr/{crId}/company-request")
    public String companyRequestForm(@PathVariable Long crId, Model model) {
        var cr = crRepository.findById(crId);
        if (cr.isEmpty()) { model.addAttribute("message", "CR not found"); return "result"; }
        var companies = companyRepository.findAll();
        model.addAttribute("crId", crId);
        model.addAttribute("companies", companies);
        return "cr/company_request";
    }

    @PostMapping("/cr/company-request")
    public String submitCompanyRequest(@RequestParam Long crId,
                                       @RequestParam Long ivId,
                                       @RequestParam Long companyId,
                                       @RequestParam String tentativeDate,
                                       @RequestParam(required = false) String alternateDate,
                                       @RequestParam Integer finalStrength,
                                       @RequestParam(required = false) String companyRequestMessage,
                                       Model model) {
        var tDt = java.time.LocalDateTime.parse(tentativeDate);
        java.time.LocalDateTime aDt = (alternateDate != null && !alternateDate.isBlank()) ? java.time.LocalDateTime.parse(alternateDate) : null;
        var iv = ivRequestService.selectCompanyAndDates(ivId, companyId, tDt, aDt, finalStrength, companyRequestMessage);
        model.addAttribute("message", iv == null ? "Failed to send request" : "Request successfully sent to Company HR!");
        return "result";
    }

    @GetMapping("/hod/iv/{ivId}/final-review")
    public String finalReviewForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "hod/final_review";
    }

    @PostMapping("/hod/iv/{ivId}/final-approve")
    public String finalApprove(@PathVariable Long ivId,
                               @RequestParam String action,
                               @RequestParam String remarks,
                               Model model) {
        var iv = ivRequestService.finalHodApproval(ivId, action, remarks);
        model.addAttribute("message", iv == null ? "IV not found" : "Date and Company selection: " + action + "D.");
        return "result";
    }

    @GetMapping("/company/iv/{ivId}/confirm")
    public String companyConfirmForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "company/confirm";
    }

    @PostMapping("/company/iv/{ivId}/confirm")
    public String companyConfirmSubmit(@PathVariable Long ivId,
                                       @RequestParam String action,
                                       @RequestParam(required = false) String alternateDate,
                                       Model model) {
        java.time.LocalDateTime aDt = (alternateDate != null && !alternateDate.isBlank()) ? java.time.LocalDateTime.parse(alternateDate) : null;
        var iv = ivRequestService.companyAction(ivId, action, aDt);
        model.addAttribute("message", iv == null ? "IV not found" : "Response processed. Status: " + iv.getStatus());
        return "result";
    }

    @GetMapping("/hod/iv/{ivId}/assign-faculty")
    public String assignFacultyForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "hod/assign_faculty";
    }

    @PostMapping("/hod/iv/{ivId}/assign-faculty")
    public String assignFacultySubmit(@PathVariable Long ivId, @RequestParam String facultyName, Model model) {
        var iv = ivRequestService.assignFaculty(ivId, facultyName);
        model.addAttribute("message", iv == null ? "IV not found" : "Faculty assigned: " + facultyName);
        return "result";
    }

    @GetMapping("/faculty/iv/{ivId}/respond")
    public String facultyRespondForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "faculty/respond";
    }

    @PostMapping("/faculty/iv/{ivId}/respond")
    public String facultyRespondSubmit(@PathVariable Long ivId, @RequestParam String action, Model model) {
        var iv = ivRequestService.facultyAction(ivId, action);
        model.addAttribute("message", iv == null ? "IV not found" : "Faculty response: " + action + "ED.");
        return "result";
    }

    @GetMapping("/cr/iv/{ivId}/logistics")
    public String logisticsForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "cr/logistics";
    }

    @PostMapping("/cr/iv/{ivId}/plan-transport")
    public String planTransportSubmit(@PathVariable Long ivId,
                                      @RequestParam Integer busCount,
                                      @RequestParam String busAllocation,
                                      @RequestParam String driverDetails,
                                      @RequestParam String pickupLocation,
                                      @RequestParam String departureTime,
                                      @RequestParam String returnTime,
                                      @RequestParam Double transportCost,
                                      Model model) {
        var dep = java.time.LocalDateTime.parse(departureTime);
        var ret = java.time.LocalDateTime.parse(returnTime);
        var iv = ivRequestService.planTransport(ivId, busCount, busAllocation, driverDetails, pickupLocation, dep, ret, transportCost);
        model.addAttribute("message", iv == null ? "IV not found" : "Transport plan saved successfully!");
        return "result";
    }

    @PostMapping("/cr/iv/{ivId}/plan-food")
    public String planFoodSubmit(@PathVariable Long ivId,
                                 @RequestParam(required = false, defaultValue = "false") Boolean foodBreakfast,
                                 @RequestParam(required = false, defaultValue = "false") Boolean foodLunch,
                                 @RequestParam(required = false, defaultValue = "false") Boolean foodSnacks,
                                 @RequestParam(required = false, defaultValue = "false") Boolean foodWater,
                                 @RequestParam Double foodCost,
                                 Model model) {
        var iv = ivRequestService.planFood(ivId, foodBreakfast, foodLunch, foodSnacks, foodWater, foodCost);
        model.addAttribute("message", iv == null ? "IV not found" : "Food plan saved successfully!");
        return "result";
    }

    @PostMapping("/cr/iv/{ivId}/plan-budget")
    public String planBudgetSubmit(@PathVariable Long ivId,
                                   @RequestParam Double entryFees,
                                   @RequestParam Double miscExpenses,
                                   Model model) {
        var iv = ivRequestService.planBudget(ivId, entryFees, miscExpenses);
        model.addAttribute("message", iv == null ? "IV not found" : "Budget prepared. Total: $" + iv.getTotalBudget());
        return "result";
    }

    @GetMapping("/hod/iv/{ivId}/verify-plan")
    public String verifyPlanForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "hod/verify_plan";
    }

    @PostMapping("/hod/iv/{ivId}/verify-plan")
    public String verifyPlanSubmit(@PathVariable Long ivId, @RequestParam String action, Model model) {
        var iv = ivRequestService.departmentalApproval(ivId, action, "");
        model.addAttribute("message", iv == null ? "IV not found" : "Verification completed. Status: " + iv.getStatus());
        return "result";
    }

    @GetMapping("/cr/iv/{ivId}/proof")
    public String uploadProofForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "cr/proof";
    }

    @PostMapping("/cr/iv/{ivId}/proof")
    public String uploadProofSubmit(@PathVariable Long ivId,
                                    @RequestParam String proofPhotosPath,
                                    @RequestParam String proofAttendancePath,
                                    @RequestParam(required = false) String proofReportPath,
                                    @RequestParam(required = false) String proofFeedback,
                                    Model model) {
        var iv = ivRequestService.uploadProof(ivId, proofPhotosPath, proofAttendancePath, proofReportPath, proofFeedback);
        model.addAttribute("message", iv == null ? "IV not found" : "Post-IV proofs uploaded successfully!");
        return "result";
    }

    @GetMapping("/hod/iv/{ivId}/verify-proof")
    public String verifyProofForm(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("iv", iv.get());
        return "hod/verify_proof";
    }

    @PostMapping("/hod/iv/{ivId}/verify-proof")
    public String verifyProofSubmit(@PathVariable Long ivId, @RequestParam String action, Model model) {
        var iv = ivRequestService.hodVerifyProof(ivId, action);
        model.addAttribute("message", iv == null ? "IV not found" : "Proof verification processed. Status: " + iv.getStatus());
        return "result";
    }

    @PostMapping("/cr/iv/{ivId}/send-request")
    public String sendRequestToCompany(@PathVariable Long ivId, Model model) {
        var iv = ivRequestService.getById(ivId);
        if (iv.isEmpty()) { model.addAttribute("message", "IV not found"); return "result"; }
        model.addAttribute("message", "Document request sent to company " + iv.get().getCompany().getName() + " successfully!");
        return "result";
    }
}
