
package com.clearanceflow.backend.dto;

import com.clearanceflow.backend.enums.ApplicationStatus;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApplicationResponseDTO {

    private Long id;
    private Long companyId;
    private Long approvalId;
    private String approvalName;

    private ApplicationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime deadline;
}
