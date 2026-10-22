
package com.clearanceflow.backend.service;

import com.clearanceflow.backend.dto.ApprovalRequestDTO;
import com.clearanceflow.backend.dto.ApprovalResponseDTO;
import com.clearanceflow.backend.entity.Approval;
import com.clearanceflow.backend.repository.ApprovalRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ApprovalService {

    private final ApprovalRepository approvalRepository;

    public ApprovalService(ApprovalRepository approvalRepository) {
        this.approvalRepository = approvalRepository;
    }

    // CREATE APPROVAL
    public ApprovalResponseDTO createApproval(ApprovalRequestDTO request) {

        Approval approval = new Approval();

        approval.setName(request.getName());
        approval.setDescription(request.getDescription());
        approval.setDepartment(request.getDepartment());
        approval.setEstimatedDays(request.getEstimatedDays());

        Approval savedApproval = approvalRepository.save(approval);

        return mapToResponseDTO(savedApproval);
    }

    // GET ALL APPROVALS
    public List<ApprovalResponseDTO> getAllApprovals() {

        return approvalRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // GET APPROVAL BY ID
    public ApprovalResponseDTO getApprovalById(Long id) {

        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Approval not found with ID: " + id
                ));

        return mapToResponseDTO(approval);
    }

    // CONVERT ENTITY TO RESPONSE DTO
    private ApprovalResponseDTO mapToResponseDTO(Approval approval) {

        ApprovalResponseDTO response = new ApprovalResponseDTO();

        response.setId(approval.getId());
        response.setName(approval.getName());
        response.setDescription(approval.getDescription());
        response.setDepartment(approval.getDepartment());
        response.setEstimatedDays(approval.getEstimatedDays());

        return response;
    }
}
