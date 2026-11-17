
package com.clearanceflow.backend.controller;

import com.clearanceflow.backend.dto.CompanyRequestDTO;
import com.clearanceflow.backend.dto.CompanyResponseDTO;
import com.clearanceflow.backend.dto.ApprovalResponseDTO;

import com.clearanceflow.backend.entity.Company;

import com.clearanceflow.backend.service.CompanyService;
import com.clearanceflow.backend.service.ComplianceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final ComplianceService complianceService;

    // CONSTRUCTOR DEPENDENCY INJECTION
    public CompanyController(
            CompanyService companyService,
            ComplianceService complianceService) {

        this.companyService = companyService;
        this.complianceService = complianceService;
    }

    // CREATE COMPANY
    @PostMapping
    public ResponseEntity<CompanyResponseDTO> createCompany(
            @Valid @RequestBody CompanyRequestDTO request) {

        CompanyResponseDTO response =
                companyService.createCompany(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET ALL COMPANIES
    @GetMapping
    public ResponseEntity<List<CompanyResponseDTO>> getAllCompanies() {

        return ResponseEntity.ok(
                companyService.getAllCompanies()
        );
    }

    // GET COMPANY BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> getCompanyById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                companyService.getCompanyById(id)
        );
    }

    // UPDATE COMPANY
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequestDTO request) {

        return ResponseEntity.ok(
                companyService.updateCompany(id, request)
        );
    }

    // DELETE COMPANY
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(
            @PathVariable Long id) {

        companyService.deleteCompany(id);

        return ResponseEntity.noContent().build();
    }

    // DAY 5: GENERATE REQUIRED APPROVAL CHECKLIST
    @GetMapping("/{id}/required-approvals")
    public ResponseEntity<List<ApprovalResponseDTO>> getRequiredApprovals(
            @PathVariable Long id) {

        // Step 1: Fetch company after checking ownership
        Company company = companyService.getAuthorizedCompany(id);

        // Step 2: Match the company against approval rules
        List<ApprovalResponseDTO> approvals =
                complianceService.getRequiredApprovals(company);

        // Step 3: Return the personalized checklist
        return ResponseEntity.ok(approvals);
    }
}
