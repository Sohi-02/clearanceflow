
package com.clearanceflow.backend.dto;

public record ApprovalDocumentRequirementDTO(
        Long id,
        String documentType,
        boolean required
) {
}
