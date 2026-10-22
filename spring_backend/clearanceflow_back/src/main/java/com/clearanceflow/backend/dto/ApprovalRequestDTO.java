
package com.clearanceflow.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalRequestDTO {

    @NotBlank(message = "Approval name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotBlank(message = "Department is required")
    @Size(max = 150)
    private String department;

    @NotNull(message = "Estimated days is required")
    @Min(value = 1, message = "Estimated days must be at least 1")
    private Integer estimatedDays;
}
