package com.sms.api.controller;

import com.sms.api.dto.demo.CreateDemoRequestDto;
import com.sms.api.entity.DemoRequest;
import com.sms.api.repository.DemoRequestRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final DemoRequestRepository demoRequestRepository;

    public PublicController(DemoRequestRepository demoRequestRepository) {
        this.demoRequestRepository = demoRequestRepository;
    }

    @PostMapping("/demo-requests")
    public ResponseEntity<Map<String, String>> submitDemoRequest(@Valid @RequestBody CreateDemoRequestDto dto) {
        DemoRequest req = new DemoRequest();
        req.setName(dto.name());
        req.setEmail(dto.email());
        req.setPhone(dto.phone());
        req.setSchoolName(dto.schoolName());
        req.setCity(dto.city());
        req.setRole(dto.role());
        req.setMessage(dto.message());
        demoRequestRepository.save(req);
        return ResponseEntity.ok(Map.of("message", "Demo request submitted successfully"));
    }
}
