
package com.clearanceflow.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentResponseDTO {

    private Long id;
    private Long applicationId;
    private String documentType;
    private String fileName;
    private LocalDateTime uploadedAt;
}
