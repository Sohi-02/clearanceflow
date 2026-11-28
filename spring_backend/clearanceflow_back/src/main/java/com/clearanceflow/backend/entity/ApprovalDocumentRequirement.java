package com.clearanceflow.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "approval_document_requirements",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"approval_id", "document_type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ApprovalDocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_id", nullable = false)
    private Approval approval;

    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @Column(nullable = false)
    private boolean required = true;
}