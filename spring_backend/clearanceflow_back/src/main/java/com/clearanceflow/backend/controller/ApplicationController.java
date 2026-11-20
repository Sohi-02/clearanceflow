
package com.clearanceflow.backend.controller;

import com.clearanceflow.backend.dto.ApplicationCreateRequestDTO;
import com.clearanceflow.backend.dto.ApplicationResponseDTO;
import com.clearanceflow.backend.service.ApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // CREATE A DRAFT APPLICATION
    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> createApplication(
            @Valid @RequestBody ApplicationCreateRequestDTO request) {

        ApplicationResponseDTO response =
                applicationService.createApplication(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET MY COMPANY'S APPLICATIONS
    @GetMapping
    public ResponseEntity<List<ApplicationResponseDTO>> getMyApplications() {

        return ResponseEntity.ok(
                applicationService.getMyApplications()
        );
    }

    // GET AN APPLICATION BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                applicationService.getApplicationById(id)
        );
    }

    // SUBMIT A DRAFT APPLICATION
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApplicationResponseDTO> submitApplication(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                applicationService.submitApplication(id)
        );
    }
}
