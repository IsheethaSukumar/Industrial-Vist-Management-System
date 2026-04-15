package com.example.ivms.service;

import com.example.ivms.entity.CR;
import com.example.ivms.repository.CRRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CRService {
    private final CRRepository crRepository;

    public CRService(CRRepository crRepository) {
        this.crRepository = crRepository;
    }

    public CR register(CR cr) {

        Optional<CR> existing = crRepository.findByRollNo(cr.getRollNo());
        if (existing.isPresent()) {
            CR e = existing.get();
            e.setName(cr.getName());
            e.setDept(cr.getDept());
            e.setEmail(cr.getEmail());
            e.setPassword(cr.getPassword());
            return crRepository.save(e);
        }
        return crRepository.save(cr);
    }

    public Optional<CR> login(String rollNo, String password) {
        return crRepository.findByRollNoAndPassword(rollNo, password);
    }

    public List<CR> findAll() {
        return crRepository.findAll();
    }
}
