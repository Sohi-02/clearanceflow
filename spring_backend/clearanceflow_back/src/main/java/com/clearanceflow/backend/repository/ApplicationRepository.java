
package com.clearanceflow.backend.repository;

import com.clearanceflow.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    // Retrieve applications belonging to a company
    //We want applicants to retrieve applications belonging to their own company.
    //For example, if Company 7 has three applications, the repository should retrieve those three records.
    //Spring Data JPA understands findByCompanyId() and generates the query automatically.
    //However, this method doesn't enforce ownership by itself. We'll check the logged-in applicant's company in ApplicationService before using it.
    List<Application> findByCompanyId(Long companyId);
}
