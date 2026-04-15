package com.example.ivms.entity;

import jakarta.persistence.*;

@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private String hrName;
    private String hrEmail;
    private String contactNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getHrName() { return hrName; }
    public void setHrName(String hrName) { this.hrName = hrName; }
    public String getHrEmail() { return hrEmail; }
    public void setHrEmail(String hrEmail) { this.hrEmail = hrEmail; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
}
