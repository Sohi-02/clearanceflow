package com.clearanceflow.backend.dto;

import com.clearanceflow.backend.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRequestDTO {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Industry is required")
    private Industry industry;

    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "Company size is required")
    private CompanySize companySize;

    @NotNull(message = "Project type is required")
    private ProjectType projectType;
}