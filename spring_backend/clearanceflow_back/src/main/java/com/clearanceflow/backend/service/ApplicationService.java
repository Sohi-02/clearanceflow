
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

    public ApplicationService(
            ApplicationRepository applicationRepository,
            ApprovalRepository approvalRepository,
            UserRepository userRepository,
            ComplianceService complianceService) {

        this.applicationRepository = applicationRepository;
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
        this.complianceService = complianceService;
    }

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

        // Only allow approvals matching this company's rules
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

    // GET ONE APPLICATION
    @Transactional(readOnly = true)
    public ApplicationResponseDTO getApplicationById(Long id) {

        Application application = getOwnedApplication(id);

        return mapToResponseDTO(application);
    }

    // SUBMIT APPLICATION
    @Transactional
    public ApplicationResponseDTO submitApplication(Long id) {

        Application application = getOwnedApplication(id);

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only DRAFT applications can be submitted"
            );
        }

        LocalDateTime submissionTime = LocalDateTime.now();

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(submissionTime);

        int estimatedDays =
                application.getApproval().getEstimatedDays();

        application.setDeadline(
                submissionTime.plusDays(estimatedDays)
        );

        Application saved = applicationRepository.save(application);

        return mapToResponseDTO(saved);
    }

    // CHECK OWNERSHIP BEFORE ACCESSING AN APPLICATION
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

    // CONVERT ENTITY TO RESPONSE DTO
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
