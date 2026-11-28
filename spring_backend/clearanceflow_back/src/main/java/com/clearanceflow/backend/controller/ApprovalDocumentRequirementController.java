
package com.clearanceflow.backend.controller;

import com.clearanceflow.backend.service.ApprovalDocumentRequirementService;
import com.clearanceflow.backend.dto.ApprovalDocumentRequirementDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalDocumentRequirementController {

    private final ApprovalDocumentRequirementService service;

    public ApprovalDocumentRequirementController(
            ApprovalDocumentRequirementService service) {

        this.service = service;
    }

    @GetMapping("/{id}/required-documents")
    public ResponseEntity<List<ApprovalDocumentRequirementDTO>>
    getRequiredDocuments(@PathVariable Long id) {

        return ResponseEntity.ok(
                service.getRequiredDocuments(id)
        );
    }
}
