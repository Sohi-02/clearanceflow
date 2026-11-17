
package com.clearanceflow.backend.service;

import com.clearanceflow.backend.dto.ApprovalResponseDTO;
import com.clearanceflow.backend.entity.Approval;
import com.clearanceflow.backend.entity.ApprovalRule;
import com.clearanceflow.backend.entity.Company;
import com.clearanceflow.backend.repository.ApprovalRuleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class ComplianceService {

    private final ApprovalRuleRepository approvalRuleRepository;

    public ComplianceService(ApprovalRuleRepository approvalRuleRepository) {
        this.approvalRuleRepository = approvalRuleRepository;
    }

    // GENERATE REQUIRED APPROVAL CHECKLIST
    @Transactional(readOnly = true)
    public List<ApprovalResponseDTO> getRequiredApprovals(Company company) {

        List<ApprovalRule> rules = approvalRuleRepository.findAll();

        // Key = approval ID, so duplicates are removed
        Map<Long, ApprovalResponseDTO> matchedApprovals =
                new LinkedHashMap<>();

        for (ApprovalRule rule : rules) {

            if (matchesRule(company, rule)) {

                Approval approval = rule.getApproval();

                matchedApprovals.putIfAbsent(
                        approval.getId(),
                        mapToResponseDTO(approval)
                );
            }
        }

        return List.copyOf(matchedApprovals.values());
    }

    // CHECK WHETHER ONE RULE MATCHES THE COMPANY
    private boolean matchesRule(Company company, ApprovalRule rule) {

        boolean industryMatches =
                rule.getIndustry() == null ||
                        rule.getIndustry() == company.getIndustry();

        boolean stateMatches =
                rule.getState() == null ||
                        (company.getState() != null &&
                                rule.getState().trim().equalsIgnoreCase(
                                        company.getState().trim()));

        boolean sizeMatches =
                rule.getCompanySize() == null ||
                        rule.getCompanySize() == company.getCompanySize();

        boolean projectMatches =
                rule.getProjectType() == null ||
                        rule.getProjectType() == company.getProjectType();

        return industryMatches &&
                stateMatches &&
                sizeMatches &&
                projectMatches;
    }

    // CONVERT APPROVAL TO RESPONSE DTO
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
