
package com.clearanceflow.backend.entity;

import com.clearanceflow.backend.enums.ApplicationStatus;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Company submitting this application
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Approval being requested
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_id", nullable = false)
    private Approval approval;

    // Current application status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    // When the draft was created
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // When the applicant submitted it
    private LocalDateTime submittedAt;

    // Estimated processing deadline
    private LocalDateTime deadline;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
