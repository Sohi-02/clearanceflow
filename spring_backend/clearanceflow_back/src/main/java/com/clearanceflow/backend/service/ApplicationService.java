
package com.clearanceflow.backend.service;

import com.clearanceflow.backend.dto.ApplicationCreateRequestDTO;
import com.clearanceflow.backend.dto.ApplicationResponseDTO;
import com.clearanceflow.backend.dto.ApprovalResponseDTO;

import com.clearanceflow.backend.entity.Application;
import com.clearanceflow.backend.entity.Approval;
import com.clearanceflow.backend.entity.Company;
import com.clearanceflow.backend.entity.User;

import com.clearanceflow.backend.enums.ApplicationStatus;

import com.clearanceflow.backend.repository.ApplicationRepository;
import com.clearanceflow.backend.repository.ApprovalRepository;
import com.clearanceflow.backend.repository.UserRepository;
import com.clearanceflow.backend.entity.ApprovalDocumentRequirement;
import com.clearanceflow.backend.repository.ApprovalDocumentRequirementRepository;
import com.clearanceflow.backend.repository.DocumentRepository;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final ComplianceService complianceService;
    private final ApprovalDocumentRequirementRepository requirementRepository;
    private final DocumentRepository documentRepository;

    // CONSTRUCTOR DEPENDENCY INJECTION
    public ApplicationService(
            ApplicationRepository applicationRepository,
            ApprovalRepository approvalRepository,
            UserRepository userRepository,
            ComplianceService complianceService,
            ApprovalDocumentRequirementRepository requirementRepository,
            DocumentRepository documentRepository) {

        this.applicationRepository = applicationRepository;
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
        this.complianceService = complianceService;
        this.requirementRepository = requirementRepository;
        this.documentRepository = documentRepository;
    }

    // =====================================================
    // APPLICANT WORKFLOW
    // =====================================================

    // GET LOGGED-IN APPLICANT'S COMPANY
    private Company getCurrentCompany() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        if (user.getCompany() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Register a company before applying"
            );
        }

        return user.getCompany();
    }

    // CREATE DRAFT APPLICATION
    @Transactional
    public ApplicationResponseDTO createApplication(
            ApplicationCreateRequestDTO request) {

        Company company = getCurrentCompany();

        Approval approval = approvalRepository
                .findById(request.getApprovalId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Approval not found"
                ));

        // Check whether this approval applies to the company
        boolean applicable = complianceService
                .getRequiredApprovals(company)
                .stream()
                .map(ApprovalResponseDTO::getId)
                .anyMatch(id -> id.equals(approval.getId()));

        if (!applicable) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This approval is not applicable to your company"
            );
        }

        Application application = new Application();

        application.setCompany(company);
        application.setApproval(approval);
        application.setStatus(ApplicationStatus.DRAFT);

        Application saved = applicationRepository.save(application);

        return mapToResponseDTO(saved);
    }

    // GET ALL APPLICATIONS OF LOGGED-IN APPLICANT
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getMyApplications() {

        Company company = getCurrentCompany();

        return applicationRepository.findByCompanyId(company.getId())
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // GET ONE APPLICATION OWNED BY THE APPLICANT
    @Transactional(readOnly = true)
    public ApplicationResponseDTO getApplicationById(Long id) {

        Application application = getOwnedApplication(id);

        return mapToResponseDTO(application);
    }

    // SUBMIT DRAFT APPLICATION
    @Transactional
    public ApplicationResponseDTO submitApplication(Long id) {

        Application application = getOwnedApplication(id);

        // Only DRAFT applications can be submitted
        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only DRAFT applications can be submitted"
            );
        }

// CHECK MANDATORY DOCUMENTS BEFORE SUBMISSION

        Long approvalId = application.getApproval().getId();

// Get document requirements for this approval
        List<ApprovalDocumentRequirement> requirements =
                requirementRepository.findByApprovalId(approvalId);

// Identify missing mandatory documents
        List<String> missingDocuments = requirements.stream()
                .filter(ApprovalDocumentRequirement::isRequired)
                .map(ApprovalDocumentRequirement::getDocumentType)
                .filter(documentType ->
                        documentRepository
                                .findByApplicationIdAndDocumentType(
                                        application.getId(),
                                        documentType
                                )
                                .isEmpty()
                )
                .toList();

        // Block submission if mandatory documents are missing
        if (!missingDocuments.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing required documents: " + missingDocuments
            );
        }


        LocalDateTime submissionTime = LocalDateTime.now();

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(submissionTime);

        int estimatedDays =
                application.getApproval().getEstimatedDays();

        // Calculate estimated processing deadline
        application.setDeadline(
                submissionTime.plusDays(estimatedDays)
        );

        Application saved = applicationRepository.save(application);

        return mapToResponseDTO(saved);
    }

    // CHECK APPLICANT OWNERSHIP
    private Application getOwnedApplication(Long id) {

        Company company = getCurrentCompany();

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Application not found"
                ));

        if (!application.getCompany().getId().equals(company.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another company's application"
            );
        }

        return application;
    }

    // =====================================================
    // OFFICER WORKFLOW - DAY 7
    // =====================================================

    // OFFICER: VIEW APPLICATIONS AWAITING OR UNDER REVIEW
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getOfficerApplications() {

        return applicationRepository.findByStatusIn(
                        List.of(
                                ApplicationStatus.SUBMITTED,
                                ApplicationStatus.UNDER_REVIEW
                        )
                )
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // OFFICER: VIEW AN INDIVIDUAL APPLICATION
    @Transactional(readOnly = true)
    public ApplicationResponseDTO getOfficerApplicationById(Long id) {

        Application application = getReviewableApplication(id);

        return mapToResponseDTO(application);
    }

    // OFFICER: START REVIEW
    @Transactional
    public ApplicationResponseDTO startReview(Long id) {

        Application application = getReviewableApplication(id);

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only submitted applications can enter review"
            );
        }

        application.setStatus(ApplicationStatus.UNDER_REVIEW);

        Application saved = applicationRepository.save(application);

        return mapToResponseDTO(saved);
    }

    // OFFICER: APPROVE APPLICATION
    @Transactional
    public ApplicationResponseDTO approveApplication(Long id) {

        Application application = getReviewableApplication(id);

        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only applications under review can be approved"
            );
        }

        application.setStatus(ApplicationStatus.APPROVED);

        Application saved = applicationRepository.save(application);

        return mapToResponseDTO(saved);
    }

    // OFFICER: REJECT APPLICATION
    @Transactional
    public ApplicationResponseDTO rejectApplication(Long id) {

        Application application = getReviewableApplication(id);

        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only applications under review can be rejected"
            );
        }

        application.setStatus(ApplicationStatus.REJECTED);

        Application saved = applicationRepository.save(application);

        return mapToResponseDTO(saved);
    }

    // HELPER: FETCH APPLICATION ACCESSIBLE TO OFFICERS
    private Application getReviewableApplication(Long id) {

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Application not found"
                ));

        // Officers cannot access unsubmitted drafts
        if (application.getStatus() == ApplicationStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Officer cannot access draft applications"
            );
        }

        return application;
    }

    // =====================================================
    // COMMON HELPER: CONVERT ENTITY TO RESPONSE DTO
    // =====================================================

    private ApplicationResponseDTO mapToResponseDTO(
            Application application) {

        ApplicationResponseDTO response = new ApplicationResponseDTO();

        response.setId(application.getId());
        response.setCompanyId(application.getCompany().getId());
        response.setApprovalId(application.getApproval().getId());
        response.setApprovalName(application.getApproval().getName());

        response.setStatus(application.getStatus());
        response.setCreatedAt(application.getCreatedAt());
        response.setSubmittedAt(application.getSubmittedAt());
        response.setDeadline(application.getDeadline());

        return response;
    }
}
