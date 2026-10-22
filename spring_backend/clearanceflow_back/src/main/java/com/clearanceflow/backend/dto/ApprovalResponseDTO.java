
package com.clearanceflow.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String department;
    private Integer estimatedDays;
}
