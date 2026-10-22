
package com.clearanceflow.backend.controller;

import com.clearanceflow.backend.dto.ApprovalRequestDTO;
import com.clearanceflow.backend.dto.ApprovalResponseDTO;
import com.clearanceflow.backend.service.ApprovalService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    // CREATE APPROVAL
    @PostMapping
    public ResponseEntity<ApprovalResponseDTO> createApproval(
            @Valid @RequestBody ApprovalRequestDTO request) {

        ApprovalResponseDTO response =
                approvalService.createApproval(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET ALL APPROVALS
    @GetMapping
    public ResponseEntity<List<ApprovalResponseDTO>> getAllApprovals() {

        return ResponseEntity.ok(
                approvalService.getAllApprovals()
        );
    }

    // GET APPROVAL BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApprovalResponseDTO> getApprovalById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                approvalService.getApprovalById(id)
        );
    }
}
