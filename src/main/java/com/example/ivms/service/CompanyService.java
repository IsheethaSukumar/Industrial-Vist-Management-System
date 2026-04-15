package com.example.ivms.service;

import com.example.ivms.entity.IVRequest;
import com.example.ivms.repository.IVRequestRepository;
import com.example.ivms.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class CompanyService {
    private final IVRequestRepository ivRequestRepository;

    public CompanyService(IVRequestRepository ivRequestRepository) {
        this.ivRequestRepository = ivRequestRepository;

    }

    public IVRequest approveOrSuggest(Long ivId, LocalDateTime dateTime) {
        Optional<IVRequest> o = ivRequestRepository.findById(ivId);
        if (o.isEmpty()) return null;
        IVRequest iv = o.get();
        iv.setTentativeDateTime(dateTime);
        iv.setStatus("COMPANY_CONFIRMED");
        return ivRequestRepository.save(iv);
    }
}

