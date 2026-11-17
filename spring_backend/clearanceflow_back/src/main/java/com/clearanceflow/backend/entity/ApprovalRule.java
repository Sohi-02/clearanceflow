
package com.clearanceflow.backend.entity;

import com.clearanceflow.backend.enums.Industry;
import com.clearanceflow.backend.enums.CompanySize;
import com.clearanceflow.backend.enums.ProjectType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "approval_rules")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NULL means any industry
    @Enumerated(EnumType.STRING)
    private Industry industry;

    // NULL means any state
    @Column(length = 100)
    private String state;

    // NULL means any company size
    @Enumerated(EnumType.STRING)
    private CompanySize companySize;

    // NULL means any project type
    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    // Each rule refers to an existing approval
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_id", nullable = false)
    private Approval approval;
}
