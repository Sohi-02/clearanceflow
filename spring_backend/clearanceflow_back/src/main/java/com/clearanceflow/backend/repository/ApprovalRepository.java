
package com.clearanceflow.backend.repository;

import com.clearanceflow.backend.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
}
