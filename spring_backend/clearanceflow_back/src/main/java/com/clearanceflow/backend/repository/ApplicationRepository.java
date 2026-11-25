
package com.clearanceflow.backend.repository;

import com.clearanceflow.backend.entity.Application;
import com.clearanceflow.backend.enums.ApplicationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    // Applicant: retrieve applications belonging to their company
    List<Application> findByCompanyId(Long companyId);

    // Officer: retrieve applications with selected statuses
    List<Application> findByStatusIn(
            List<ApplicationStatus> statuses
    );
}
