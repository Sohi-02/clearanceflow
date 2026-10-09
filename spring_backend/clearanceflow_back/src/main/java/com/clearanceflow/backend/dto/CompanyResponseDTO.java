package com.clearanceflow.backend.dto;

import com.clearanceflow.backend.enums.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyResponseDTO {

    private Long id;
    private String name;
    private Industry industry;
    private String state;
    private CompanySize companySize;
    private ProjectType projectType;
}