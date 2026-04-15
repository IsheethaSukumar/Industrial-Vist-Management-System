package com.example.ivms.restcontroller;

import com.example.ivms.entity.IVRequest;
import com.example.ivms.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
public class CompanyRestController {

    private final CompanyService companyService;

    public CompanyRestController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/iv/{id}/confirm")
    public IVRequest confirm(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String dt = body.get("dateTime");
        return companyService.approveOrSuggest(id, LocalDateTime.parse(dt));
    }

}
