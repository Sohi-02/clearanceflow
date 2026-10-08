package com.clearanceflow.backend.repository;

import com.clearanceflow.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}