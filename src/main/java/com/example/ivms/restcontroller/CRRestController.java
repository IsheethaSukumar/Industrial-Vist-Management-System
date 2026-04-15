package com.example.ivms.restcontroller;

import com.example.ivms.entity.*;
import com.example.ivms.repository.CRRepository;
import com.example.ivms.service.CRService;
import com.example.ivms.service.IVRequestService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cr")
public class CRRestController {

    private final CRService crService;
    private final IVRequestService ivRequestService;
    private final CRRepository crRepository;

    public CRRestController(CRService crService, IVRequestService ivRequestService, CRRepository crRepository) {
        this.crService = crService;
        this.ivRequestService = ivRequestService;
        this.crRepository = crRepository;
    }

    @PostMapping("/register")
    public CR register(@RequestBody CR cr) {
        return crService.register(cr);
    }

    @PostMapping("/login")
    public CR login(@RequestBody Map<String, String> body) {
        String roll = body.get("rollNo");
        String pass = body.get("password");
        Optional<CR> c = crService.login(roll, pass);
        return c.orElse(null);
    }

    @PostMapping("/iv/create")
    public IVRequest create(@RequestParam Long crId, @RequestParam Long companyId) {
        Optional<CR> cr = crRepository.findById(crId);
        if (cr.isEmpty()) return null;
        return ivRequestService.create(cr.get(), companyId);
    }

    @PostMapping("/iv/{id}/invite-staff")
    public IVRequest invite(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String invited = body.getOrDefault("invitedStaff", "");
        return ivRequestService.inviteStaff(id, invited);
    }

    @PostMapping("/iv/{id}/tentative")
    public IVRequest tentative(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String dt = body.get("dateTime");
        return ivRequestService.setTentative(id, LocalDateTime.parse(dt));
    }

    @PostMapping("/iv/{id}/submit")
    public IVRequest submit(@PathVariable Long id) {
        return ivRequestService.submitForApproval(id);
    }

    @PostMapping("/iv/{id}/hr-confirmation")
    public IVRequest hrConfirm(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String path = body.get("path");
        return ivRequestService.uploadHRConfirmation(id, path);
    }

    @PostMapping("/iv/{id}/students")
    public List<Student> addStudents(@PathVariable Long id, @RequestBody List<Student> students) {
        return ivRequestService.addStudents(id, students);
    }

    @GetMapping("/iv/{id}")
    public IVRequest getIv(@PathVariable Long id) {
        return ivRequestService.getById(id).orElse(null);
    }

    @GetMapping("/iv")
    public List<IVRequest> all() {
        return ivRequestService.findAll();
    }
}
