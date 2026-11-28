
package com.clearanceflow.backend.service;

import com.clearanceflow.backend.dto.ApprovalDocumentRequirementDTO;
import com.clearanceflow.backend.repository.ApprovalRepository;
import com.clearanceflow.backend.repository.ApprovalDocumentRequirementRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ApprovalDocumentRequirementService {

    private final ApprovalDocumentRequirementRepository requirementRepository;
    private final ApprovalRepository approvalRepository;

    public ApprovalDocumentRequirementService(
            ApprovalDocumentRequirementRepository requirementRepository,
            ApprovalRepository approvalRepository) {

        this.requirementRepository = requirementRepository;
        this.approvalRepository = approvalRepository;
    }

    public List<ApprovalDocumentRequirementDTO> getRequiredDocuments(
            Long approvalId) {

        if (!approvalRepository.existsById(approvalId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Approval not found"
            );
        }

        return requirementRepository.findByApprovalId(approvalId)
                .stream()
                .map(requirement ->
                        new ApprovalDocumentRequirementDTO(
                                requirement.getId(),
                                requirement.getDocumentType(),
                                requirement.isRequired()
                        )
                )
                .toList();
    }
}
