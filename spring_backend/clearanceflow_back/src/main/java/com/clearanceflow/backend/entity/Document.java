
package com.clearanceflow.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Example: BUILDING_PLAN, OWNERSHIP_PROOF
    @Column(nullable = false, length = 100)
    private String documentType;

    // Original name shown to the applicant
    @Column(nullable = false, length = 255)
    private String fileName;

    // Private storage location, not a public URL
    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    // The application to which this document belongs
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
