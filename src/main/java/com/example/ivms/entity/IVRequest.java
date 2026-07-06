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

    // New workflow fields
    private String department;
    private String yearAndSection;
    private Integer numberOfStudents;
    private String purpose;
    private String preferredDomain;
    private LocalDateTime alternateDate;
    private String facultyInCharge;
    private String facultyStatus; // "PENDING", "ACCEPTED", "DECLINED"
    private String companyRequestMessage;

    // Transport
    private Integer busCount;
    private String busAllocation;
    private String driverDetails;
    private String pickupLocation;
    private LocalDateTime departureTime;
    private LocalDateTime returnTime;

    // Food
    private Boolean foodBreakfast;
    private Boolean foodLunch;
    private Boolean foodSnacks;
    private Boolean foodWater;

    // Budget
    private Double transportCost;
    private Double foodCost;
    private Double entryFees;
    private Double miscExpenses;
    private Double totalBudget;

    // Post-IV proof
    private String proofPhotosPath;
    private String proofAttendancePath;
    private String proofReportPath;
    private String proofFeedback;

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

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getYearAndSection() { return yearAndSection; }
    public void setYearAndSection(String yearAndSection) { this.yearAndSection = yearAndSection; }
    public Integer getNumberOfStudents() { return numberOfStudents; }
    public void setNumberOfStudents(Integer numberOfStudents) { this.numberOfStudents = numberOfStudents; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getPreferredDomain() { return preferredDomain; }
    public void setPreferredDomain(String preferredDomain) { this.preferredDomain = preferredDomain; }
    public LocalDateTime getAlternateDate() { return alternateDate; }
    public void setAlternateDate(LocalDateTime alternateDate) { this.alternateDate = alternateDate; }
    public String getFacultyInCharge() { return facultyInCharge; }
    public void setFacultyInCharge(String facultyInCharge) { this.facultyInCharge = facultyInCharge; }
    public String getFacultyStatus() { return facultyStatus; }
    public void setFacultyStatus(String facultyStatus) { this.facultyStatus = facultyStatus; }

    public Integer getBusCount() { return busCount; }
    public void setBusCount(Integer busCount) { this.busCount = busCount; }
    public String getBusAllocation() { return busAllocation; }
    public void setBusAllocation(String busAllocation) { this.busAllocation = busAllocation; }
    public String getDriverDetails() { return driverDetails; }
    public void setDriverDetails(String driverDetails) { this.driverDetails = driverDetails; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }

    public Boolean getFoodBreakfast() { return foodBreakfast; }
    public void setFoodBreakfast(Boolean foodBreakfast) { this.foodBreakfast = foodBreakfast; }
    public Boolean getFoodLunch() { return foodLunch; }
    public void setFoodLunch(Boolean foodLunch) { this.foodLunch = foodLunch; }
    public Boolean getFoodSnacks() { return foodSnacks; }
    public void setFoodSnacks(Boolean foodSnacks) { this.foodSnacks = foodSnacks; }
    public Boolean getFoodWater() { return foodWater; }
    public void setFoodWater(Boolean foodWater) { this.foodWater = foodWater; }

    public Double getTransportCost() { return transportCost; }
    public void setTransportCost(Double transportCost) { this.transportCost = transportCost; }
    public Double getFoodCost() { return foodCost; }
    public void setFoodCost(Double foodCost) { this.foodCost = foodCost; }
    public Double getEntryFees() { return entryFees; }
    public void setEntryFees(Double entryFees) { this.entryFees = entryFees; }
    public Double getMiscExpenses() { return miscExpenses; }
    public void setMiscExpenses(Double miscExpenses) { this.miscExpenses = miscExpenses; }
    public Double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(Double totalBudget) { this.totalBudget = totalBudget; }

    public String getProofPhotosPath() { return proofPhotosPath; }
    public void setProofPhotosPath(String proofPhotosPath) { this.proofPhotosPath = proofPhotosPath; }
    public String getProofAttendancePath() { return proofAttendancePath; }
    public void setProofAttendancePath(String proofAttendancePath) { this.proofAttendancePath = proofAttendancePath; }
    public String getProofReportPath() { return proofReportPath; }
    public void setProofReportPath(String proofReportPath) { this.proofReportPath = proofReportPath; }
    public String getProofFeedback() { return proofFeedback; }
    public void setProofFeedback(String proofFeedback) { this.proofFeedback = proofFeedback; }

    public String getCompanyRequestMessage() { return companyRequestMessage; }
    public void setCompanyRequestMessage(String companyRequestMessage) { this.companyRequestMessage = companyRequestMessage; }

    private String studentsPdfPath;
    public String getStudentsPdfPath() { return studentsPdfPath; }
    public void setStudentsPdfPath(String studentsPdfPath) { this.studentsPdfPath = studentsPdfPath; }
}
