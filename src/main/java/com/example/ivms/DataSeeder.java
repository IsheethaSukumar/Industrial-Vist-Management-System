package com.example.ivms;

import com.example.ivms.entity.Company;
import com.example.ivms.repository.CompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;

    public DataSeeder(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (companyRepository.count() == 0) {
            seedCompany("Google", "Bangalore", "John Doe", "john.doe@google.com", "9876543210", "IT", "Available");
            seedCompany("Microsoft", "Hyderabad", "Jane Smith", "jane.smith@microsoft.com", "9876543211", "IT", "Available");
            seedCompany("TCS", "Chennai", "Ram Kumar", "ram.kumar@tcs.com", "9876543212", "IT & Services", "Available");
            seedCompany("Infosys", "Mysore", "Priya Sen", "priya.sen@infosys.com", "9876543213", "Software Engineering", "Available");
            seedCompany("Wipro", "Bangalore", "Amit Patel", "amit.patel@wipro.com", "9876543214", "Consulting", "Available");
        }
    }

    private void seedCompany(String name, String location, String hrName, String hrEmail, String contactNo, String domain, String availability) {
        Company company = new Company();
        company.setName(name);
        company.setLocation(location);
        company.setHrName(hrName);
        company.setHrEmail(hrEmail);
        company.setContactNo(contactNo);
        company.setDomain(domain);
        company.setAvailability(availability);
        companyRepository.save(company);
    }
}
