package com.example.ivms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class IVRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CR cr;

    @ManyToOne
    private Company company;

    private LocalDateTime tentativeDateTime;
    private String invitedStaff; // comma separated names/emails
    private String status;
    private String hrConfirmationPath;
    private String transportDetails;
    private Integer studentLimit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CR getCr() { return cr; }
    public void setCr(CR cr) { this.cr = cr; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public LocalDateTime getTentativeDateTime() { return tentativeDateTime; }
    public void setTentativeDateTime(LocalDateTime tentativeDateTime) { this.tentativeDateTime = tentativeDateTime; }
    public String getInvitedStaff() { return invitedStaff; }
    public void setInvitedStaff(String invitedStaff) { this.invitedStaff = invitedStaff; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHrConfirmationPath() { return hrConfirmationPath; }
    public void setHrConfirmationPath(String hrConfirmationPath) { this.hrConfirmationPath = hrConfirmationPath; }
    public String getTransportDetails() { return transportDetails; }
    public void setTransportDetails(String transportDetails) { this.transportDetails = transportDetails; }
    public Integer getStudentLimit() { return studentLimit; }
    public void setStudentLimit(Integer studentLimit) { this.studentLimit = studentLimit; }
}
