
package com.clearanceflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationCreateRequestDTO {

    @NotNull(message = "Approval ID is required")
    private Long approvalId;
}
