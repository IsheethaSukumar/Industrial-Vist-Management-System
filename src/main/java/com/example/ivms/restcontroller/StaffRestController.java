package com.example.ivms.restcontroller;

import com.example.ivms.entity.Approval;
import com.example.ivms.service.ApprovalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffRestController {

    private final ApprovalService approvalService;

    public StaffRestController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/iv/{ivId}/stage")
    public Approval createStage(@PathVariable Long ivId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        return approvalService.createPending(ivId, role);
    }

    @PostMapping("/approval/{id}/act")
    public Approval act(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String approver = body.getOrDefault("approverName", "");
        String status = body.getOrDefault("status", "PENDING");
        String remarks = body.getOrDefault("remarks", "");
        return approvalService.act(id, approver, status, remarks);
    }

    @GetMapping("/approvals")
    public List<Approval> all() {
        return approvalService.findAll();
    }
}
