package com.clearanceflow.backend.repository;

import com.clearanceflow.backend.entity.ApprovalDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalDocumentRequirementRepository
        extends JpaRepository<ApprovalDocumentRequirement, Long> {

    List<ApprovalDocumentRequirement> findByApprovalId(Long approvalId);
}