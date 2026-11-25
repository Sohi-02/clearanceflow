
package com.clearanceflow.backend.controller;

import com.clearanceflow.backend.dto.ApplicationResponseDTO;
import com.clearanceflow.backend.service.ApplicationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officer/applications")
public class OfficerApplicationController {

    private final ApplicationService applicationService;

    public OfficerApplicationController(
            ApplicationService applicationService) {

        this.applicationService = applicationService;
    }

    // OFFICER: VIEW SUBMITTED AND UNDER-REVIEW APPLICATIONS
    @GetMapping
    public ResponseEntity<List<ApplicationResponseDTO>>
    getOfficerApplications() {

        return ResponseEntity.ok(
                applicationService.getOfficerApplications()
        );
    }

    // OFFICER: OPEN AN APPLICATION
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                applicationService.getOfficerApplicationById(id)
        );
    }

    // OFFICER: START REVIEW
    @PatchMapping("/{id}/start-review")
    public ResponseEntity<ApplicationResponseDTO> startReview(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                applicationService.startReview(id)
        );
    }

    // OFFICER: APPROVE APPLICATION
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApplicationResponseDTO> approveApplication(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                applicationService.approveApplication(id)
        );
    }

    // OFFICER: REJECT APPLICATION
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponseDTO> rejectApplication(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                applicationService.rejectApplication(id)
        );
    }
}
