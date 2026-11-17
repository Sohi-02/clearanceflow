
package com.clearanceflow.backend.repository;

import com.clearanceflow.backend.entity.ApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRuleRepository
        extends JpaRepository<ApprovalRule, Long> {
}
