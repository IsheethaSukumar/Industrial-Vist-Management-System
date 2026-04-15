package com.example.ivms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private IVRequest iv;

    private String role;
    private String approverName;
    private LocalDateTime approvalDate;
    private String remarks;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public IVRequest getIv() { return iv; }
    public void setIv(IVRequest iv) { this.iv = iv; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
